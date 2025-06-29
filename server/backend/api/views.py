from rest_framework.response import Response
from rest_framework.decorators import api_view, permission_classes
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema
from rest_framework.authtoken.models import Token
from django.contrib.auth.models import User
from drf_yasg import openapi
from polaris.models import *
from .serializers import *
from django.contrib.auth.password_validation import validate_password
from django.contrib.auth import authenticate
from rest_framework.permissions import IsAuthenticated
from utils.sms import *
from django.core.cache import cache
from django.http import JsonResponse
from django.contrib.auth import login
from rest_framework.authtoken.models import Token
from django.contrib.auth import logout
from django.shortcuts import get_object_or_404
from datetime import timedelta
from django.utils import timezone

TEST_SERIALIZER_MAP = {
    'http_download': HTTPDownloadTestSerializer,
    'http_upload': HTTPUploadTestSerializer,
    'ping': PingTestSerializer,
    'dns': DNSTestSerializer,
    'web': WebTestSerializer,
    'sms': SMSTestSerializer,
    'item': ItemSerializer,
}

@swagger_auto_schema(method='post', request_body=RequestOTPSerializer)
@api_view(['POST'])
def request_otp(request):
    serializer = RequestOTPSerializer(data=request.data)
    if not serializer.is_valid():
        return JsonResponse(serializer.errors, status=400)

    phone_number = serializer.validated_data['phone_number']

    try:
        user = User.objects.get(phone_number=phone_number)
    except User.DoesNotExist:
        return JsonResponse({
            'status': 'error',
            'message': 'User with this phone number does not exist.'
        }, status=400)

    if user.is_verified:
        return JsonResponse({
            'status': 'error',
            'message': 'User is already verified.'
        }, status=400)

    code, _ = send_otp(phone_number)
    cache_key = f"otp_{phone_number}"
    cache.set(cache_key, code, timeout=300)

    return JsonResponse({
        'status': 'success',
        'message': 'OTP sent successfully',
        'phone_number': str(phone_number),
        'otp': code  # remove this in production!
    })


@swagger_auto_schema(method='post', request_body=VerifyOTPSerializer)
@api_view(['POST'])
def verify_otp(request):
    serializer = VerifyOTPSerializer(data=request.data)
    if not serializer.is_valid():
        return JsonResponse(serializer.errors, status=400)

    phone_number = str(serializer.validated_data['phone_number'])
    otp_code = serializer.validated_data['otp_code']

    cache_key = f"otp_{phone_number}"
    cached_otp = cache.get(cache_key)

    if not cached_otp:
        return JsonResponse({
            'status': 'error',
            'message': 'OTP expired or not requested.'
        }, status=400)

    if cached_otp != otp_code:
        return JsonResponse({
            'status': 'error',
            'message': 'Invalid OTP.'
        }, status=400)

    try:
        user = User.objects.get(phone_number=phone_number)
    except User.DoesNotExist:
        return JsonResponse({
            'status': 'error',
            'message': 'User does not exist.'
        }, status=400)

    user.is_verified = True
    user.save()
    cache.delete(cache_key)

    return JsonResponse({
        'status': 'success',
        'message': 'OTP verified. User is now verified.',
        'user_id': user.id,
        'username': user.username
    })


@swagger_auto_schema(method='post', request_body=SignupSerializer)
@api_view(['POST'])
def signup_user(request):
    serializer = SignupSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    user = serializer.save()
    user.is_verified = False
    user.save()

    return JsonResponse({
        'status': 'success',
        'message': 'User addd successfully. Please verify with OTP.',
        'user_id': user.id,
        'username': user.username
    })


@swagger_auto_schema(method='post', request_body=LoginSerializer)
@api_view(['POST'])
def login_user(request):
    serializer = LoginSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    user = serializer.validated_data['user']
    login(request, user)  

    token, _ = Token.objects.get_or_create(user=user)

    return Response({
        "message": "Login successful",
        "token": token.key,
        "user": {
            "username": user.username,
            "phone_number": user.phone_number,
        }
    })


@permission_classes([IsAuthenticated])
@api_view(['POST'])
def logout_user(request):
    logout(request)
    return Response({"message": "Logged out successfully."})


@swagger_auto_schema(method='post', request_body=CellInfoSerializer)
@api_view(['POST'])
def add_cell_info(request):
    serializer = CellInfoSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=AddTestInputSerializer)
@api_view(['POST'])
def add_test(request):
    type_ = request.data.get('type_')
    if not type_ or type_ not in TEST_SERIALIZER_MAP:
        return Response({'error': 'Invalid or missing test type'}, status=400)

    try:
        user = User.objects.get(phone_number=request.data.get('phone_number'))
    except User.DoesNotExist:
        return Response({'error': 'User not found with given phone number'}, status=400)

    try:
        cell_info = CellInfo.objects.get(id=request.data.get('cell_info'))
    except CellInfo.DoesNotExist:
        return Response({'error': 'CellInfo not found with given ID'}, status=400)

    test = Test.objects.create(
        phone_number=user,
        timestamp=request.data.get('timestamp'),
        cell_info=cell_info
    )

    subtype_data = dict(request.data.get('detail', {}))
    subtype_data['id'] = test.id

    serializer_class = TEST_SERIALIZER_MAP[type_]
    serializer = serializer_class(data=subtype_data)

    if serializer.is_valid():
        serializer.save()
        return Response(UnifiedTestSerializer(test).data, status=status.HTTP_201_CREATED)
    else:
        test.delete()
        return Response(serializer.errors, status=400)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_users(request):
    users = User.objects.all()
    serializer = UserSerializer(users, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_cell_info(request):
    data = CellInfo.objects.all()
    serializer = CellInfoSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get', responses={200: UnifiedTestSerializer(many=True)})
@api_view(['GET'])
def get_tests(request):
    time_filter = request.query_params.get('range')
    query = Test.objects.all()

    now = timezone.now()
    if time_filter:
        time_filter = time_filter.lower()
        if time_filter.endswith('h'):  
            try:
                hours = float(time_filter[:-1])
                time_threshold = now - timedelta(hours=hours)
                query = query.filter(timestamp__gte=time_threshold)
            except ValueError:
                return Response({"error": "Invalid hour format. Use '1h', '3h', etc."}, status=400)
        elif time_filter.endswith('d'):  
            try:
                days = int(time_filter[:-1])
                time_threshold = now - timedelta(days=days)
                query = query.filter(timestamp__gte=time_threshold)
            except ValueError:
                return Response({"error": "Invalid day format. Use '1d' for 1 day."}, status=400)
        elif time_filter.endswith('w'): 
            try:
                weeks = int(time_filter[:-1])
                time_threshold = now - timedelta(weeks=weeks)
                query = query.filter(timestamp__gte=time_threshold)
            except ValueError:
                return Response({"error": "Invalid week format. Use '1w' for 1 week."}, status=400)
        else:
            return Response({"error": "Invalid range. Use formats like '1h', '1d', '1w'."}, status=400)

    serializer = UnifiedTestSerializer(query, many=True)
    return Response(serializer.data)



@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_item(request):
    items = Item.objects.all()
    serializer = ItemSerializer(items, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='post', request_body=ItemSerializer)
@api_view(['POST'])
def add_item(request):
    serializer = ItemSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data)
    return Response(serializer.errors, status=400)