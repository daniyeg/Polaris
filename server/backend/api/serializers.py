from rest_framework import serializers
from polaris.models import *
# from django.contrib.auth.models import User
from django.core.cache import cache
from utils.check_password import *
from django.contrib.auth import authenticate


class RequestOTPSerializer(serializers.Serializer):
    phone_number = serializers.CharField(
        max_length=15,
        required=True,
    )

    def validate(self, data):
        phone = data['phone_number']

        if User.objects.filter(phone_number=phone).exists():
            raise serializers.ValidationError({
                "phone_number": "A user with this phone number already exists."
            })

        return data



class VerifyOTPSerializer(serializers.Serializer):
    phone_number = serializers.CharField(max_length=15, required=True)
    otp_code = serializers.CharField(max_length=6, required=True)


class ItemSerializer(serializers.ModelSerializer):
    class Meta:
        model = Item
        fields = '__all__'


class SignupSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    otp_code = serializers.CharField(write_only=True, required=True)
    phone_number = serializers.CharField(max_length=15, required=True)

    class Meta:
        model = User
        fields = ['username', 'password', 'phone_number', 'otp_code']

    def validate(self, data):
        cache_key = f"otp_{data['phone_number']}"
        cached_otp = cache.get(cache_key)
        
        if not cached_otp:
            raise serializers.ValidationError({"otp_code": "OTP expired or not requested"})
        
        if cached_otp != data['otp_code']:
            raise serializers.ValidationError({"otp_code": "Invalid OTP"})
        
        if check_password_safety(data['password']):
            return data

    def create(self, validated_data):
        validated_data.pop('otp_code')
        
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data.get('email'),
            password=validated_data['password'],
            phone_number=validated_data['phone_number']
        )
        
        cache.delete(f"otp_{validated_data['phone_number']}")
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
