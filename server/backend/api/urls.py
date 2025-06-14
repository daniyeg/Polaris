from django.urls import path, re_path
from . import views


urlpatterns = [
    path('', views.get_item),
    path('api/add/', views.add_item),
    path('api/request_otp/', views.request_otp),
    path('api/verify_otp/', views.verify_otp),

    path('api/signup/', views.signup_user),
    path('api/login/', views.login_user),
    path('api/logout/', views.logout_user),

]
