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

    token, _ = Token.objects.get_or_add(user=user)

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


@swagger_auto_schema(method='post', request_body=TestSerializer)
@api_view(['POST'])
def add_test(request):
    serializer = TestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)

@swagger_auto_schema(method='post', request_body=HTTPDownloadTestSerializer)
@api_view(['POST'])
def add_http_download_test(request):
    serializer = HTTPDownloadTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=HTTPDownloadTestSerializer)
@api_view(['POST'])
def add_http_upload_test(request):
    serializer = HTTPUploadTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=PingTestSerializer)
@api_view(['POST'])
def add_ping_test(request):
    serializer = PingTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=DNSTestSerializer)
@api_view(['POST'])
def add_dns_test(request):
    serializer = DNSTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=WebTestSerializer)
@api_view(['POST'])
def add_web_test(request):
    serializer = WebTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
    return Response(serializer.errors, status=400)


@swagger_auto_schema(method='post', request_body=SMSTestSerializer)
@api_view(['POST'])
def add_sms_test(request):
    serializer = SMSTestSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response(serializer.data, status=201)
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


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_tests(request):
    data = Test.objects.all()
    serializer = TestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_http_download_tests(request):
    data = HttpDownloadTest.objects.all()
    serializer = HttpDownloadTestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_http_upload_tests(request):
    data = HttpUploadTest.objects.all()
    serializer = HttpUploadTestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_ping_tests(request):
    data = PingTest.objects.all()
    serializer = PingTestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_dns_tests(request):
    data = DnsTest.objects.all()
    serializer = DnsTestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_web_tests(request):
    data = WebTest.objects.all()
    serializer = WebTestSerializer(data, many=True)
    return Response(serializer.data)


@swagger_auto_schema(method='get')
@api_view(['GET'])
def get_sms_tests(request):
    data = SmsTest.objects.all()
    serializer = SmsTestSerializer(data, many=True)
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