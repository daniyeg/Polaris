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
    class Meta:
        model = CellInfo
        fields = '__all__'


class TestSerializer(serializers.ModelSerializer):
    class Meta:
        model = Test
        fields = '__all__'


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