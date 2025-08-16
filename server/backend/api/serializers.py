from rest_framework import serializers
from polaris.models import *
# from django.contrib.auth.models import User
from django.core.cache import cache
from utils.check_password import *
from django.contrib.auth import authenticate


class RequestOTPSerializer(serializers.Serializer):
    phone_number = serializers.CharField(max_length=15, required=True)

    def validate(self, data):
        phone = data['phone_number']
        if not User.objects.filter(phone_number=phone).exists():
            raise serializers.ValidationError({
                "phone_number": "No user with this phone number exists."
            })
        return data

class GetPhoneSerializer(serializers.Serializer):
    username = serializers.CharField(max_length=15, required=True)

    def validate(self, data):
        username = data['username']
        if not User.objects.filter(username=username).exists():
            raise serializers.ValidationError({
                "username": "No user with this username exists."
            })
        return data


class VerifyOTPSerializer(serializers.Serializer):
    phone_number = serializers.CharField(max_length=15, required=True)
    otp_code = serializers.CharField(max_length=6, required=True)


class SignupSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    phone_number = serializers.CharField(max_length=15, required=True)

    class Meta:
        model = User
        fields = ['username', 'password', 'phone_number']

    def validate(self, data):
        phone = data['phone_number']
        if User.objects.filter(phone_number=phone).exists():
            raise serializers.ValidationError({
                "error": "A user with this phone number exists."
            })

        username = data['username']
        if User.objects.filter(username=username).exists():
            raise serializers.ValidationError({
                "error": "A user with this username exists."
            })

        if check_password_safety(data['password']):
            return data

        
    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            password=validated_data['password'],
            phone_number=validated_data['phone_number'],
        )
        user.is_verified = False
        user.save()
        return user




class LoginSerializer(serializers.Serializer):
    identifier = serializers.CharField(help_text="Username or phone number")
    password = serializers.CharField(write_only=True)

    def validate(self, data):
        identifier = data['identifier']
        password = data['password']

        from django.contrib.auth import get_user_model, authenticate

        User = get_user_model()

        try:
            user = User.objects.get(phone_number=identifier)
        except User.DoesNotExist:
            try:
                user = User.objects.get(username=identifier)
            except User.DoesNotExist:
                raise serializers.ValidationError("Invalid credentials")

        if not user.check_password(password):
            raise serializers.ValidationError("Invalid credentials")

        data['user'] = user
        return data


class CellInfoSerializer(serializers.ModelSerializer):
    phone_number = serializers.CharField(required=True)  
    lat = serializers.FloatField(required=True)
    lng = serializers.FloatField(required=True)
    timestamp = serializers.DateTimeField(required=True)
    gen = serializers.CharField(max_length=20, required=True)
    tech = serializers.CharField(max_length=50, required=True)
    plmn = serializers.CharField(max_length=20, required=True)
    cid = serializers.IntegerField(required=True)

    class Meta:
        model = CellInfo
        fields = '__all__'
        extra_kwargs = {
            'lac': {'required': False},
            'rac': {'required': False},
            'tac': {'required': False},
            'freq_band': {'required': False},
            'afrn': {'required': False},
            'freq': {'required': False},
            'rsrp': {'required': False},
            'rsrq': {'required': False},
            'rscp': {'required': False},
            'ecno': {'required': False},
            'rxlev': {'required': False},
        }

    def create(self, validated_data):
        phone_str = validated_data.pop('phone_number')
        try:
            user = User.objects.get(phone_number=phone_str)
        except User.DoesNotExist:
            raise serializers.ValidationError({'phone_number': 'User with this phone number does not exist.'})
        
        validated_data['phone_number'] = user
        return super().create(validated_data)


class HTTPDownloadTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = HTTPDownloadTest
        fields = '__all__'


class HTTPUploadTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = HTTPUploadTest
        fields = '__all__'


class PingTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = PingTest
        fields = '__all__'


class DNSTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = DNSTest
        fields = '__all__'


class WebTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = WebTest
        fields = '__all__'


class SMSTestSerializer(serializers.ModelSerializer):
    class Meta:
        model = SMSTest
        fields = '__all__'


class ItemSerializer(serializers.ModelSerializer):
    class Meta:
        model = Item
        fields = '__all__'

class UnifiedTestSerializer(serializers.ModelSerializer):
    type_ = serializers.SerializerMethodField()
    detail = serializers.SerializerMethodField()

    class Meta:
        model = Test
        fields = ['id', 'phone_number', 'timestamp', 'cell_info', 'type_', 'detail']

    def get_type_(self, obj):
        if hasattr(obj, 'httpdownloadtest'):
            return 'http_download'
        elif hasattr(obj, 'httpuploadtest'):
            return 'http_upload'
        elif hasattr(obj, 'pingtest'):
            return 'ping'
        elif hasattr(obj, 'dnstest'):
            return 'dns'
        elif hasattr(obj, 'webtest'):
            return 'web'
        elif hasattr(obj, 'smstest'):
            return 'sms'
        return 'unknown'

    def get_detail(self, obj):
        if hasattr(obj, 'httpdownloadtest'):
            return HTTPDownloadTestSerializer(obj.httpdownloadtest).data
        elif hasattr(obj, 'httpuploadtest'):
            return HTTPUploadTestSerializer(obj.httpuploadtest).data
        elif hasattr(obj, 'pingtest'):
            return PingTestSerializer(obj.pingtest).data
        elif hasattr(obj, 'dnstest'):
            return DNSTestSerializer(obj.dnstest).data
        elif hasattr(obj, 'webtest'):
            return WebTestSerializer(obj.webtest).data
        elif hasattr(obj, 'smstest'):
            return SMSTestSerializer(obj.smstest).data
        return None

class AddTestInputSerializer(serializers.Serializer):
    type_ = serializers.ChoiceField(choices=[
        ('http_download', 'http_download'),
        ('http_upload', 'http_upload'),
        ('ping', 'ping'),
        ('dns', 'dns'),
        ('web', 'web'),
        ('sms', 'sms'),
        ('item', 'item'),
        
    ],required=True)
    phone_number = serializers.CharField(required=True)
    timestamp = serializers.DateTimeField(required=True)
    cell_info = serializers.IntegerField(required=True)
    detail = serializers.DictField(required=True)