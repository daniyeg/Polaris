from django.urls import path, re_path
from . import views


urlpatterns = [
    path('', views.get_item),
    path('add/', views.add_item),
    path('request_otp/', views.request_otp),
    path('verify_otp/', views.verify_otp),

    path('signup/', views.signup_user),
    path('login/', views.login_user),
    path('logout/', views.logout_user),

]
