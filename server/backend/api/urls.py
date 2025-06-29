from django.urls import path, re_path
from . import views


urlpatterns = [
    path('', views.get_item, name='get-item'),
    path('add/', views.add_item, name='add-item'),
    path('request_otp/', views.request_otp, name='request-otp'),
    path('verify_otp/', views.verify_otp, name='verify-otp'),
    
    path('signup/', views.signup_user, name='signup'),
    path('login/', views.login_user, name='login'),
    path('logout/', views.logout_user, name='logout'),
]
