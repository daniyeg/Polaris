from django.urls import path, re_path, include
from rest_framework import permissions
from drf_yasg.views import get_schema_view
from drf_yasg import openapi
from django.contrib import admin

schema_view = get_schema_view(
    openapi.Info(
        title="Polaris API",
        default_version='v1',
    ),
    public=True,
    url='https://polaris-server-30ha.onrender.com',  # Force HTTPS
)



urlpatterns = [
   path('admin/', admin.site.urls),
   # path('k', include('polaris.urls')),
   path('api', include('api.urls')),
    
   re_path(r'^swagger(?P<format>\.json|\.yaml)$', schema_view.without_ui(cache_timeout=0), name='schema-json'),
   path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
   path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
   # re_path(r'^swagger(?P<format>\.json|\.yaml)$', 
   #          schema_view.without_ui(cache_timeout=0), name='schema-json'),
   #  path('swagger/', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),
   #  path('redoc/', schema_view.with_ui('redoc', cache_timeout=0), name='schema-redoc'),
]
