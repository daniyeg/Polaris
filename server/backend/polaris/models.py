from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    phone_number = models.CharField(max_length=15, unique=True, blank=False, null=False)
    is_verified = models.BooleanField(default=False)
    
    username = models.CharField(max_length=150, unique=True, blank=True, null=True)
    
    USERNAME_FIELD = 'phone_number'


class Item(models.Model):
    name = models.CharField(max_length=200)
    created = models.DateTimeField(auto_now_add=True)


class CellInfo(models.Model):
    id = models.AutoField(primary_key=True)
    phone_number = models.ForeignKey(User, on_delete=models.CASCADE, to_field='phone_number')
    lat = models.FloatField()
    lng = models.FloatField()
    timestamp = models.DateTimeField()
    gen = models.CharField(max_length=20)
    tech = models.CharField(max_length=50)
    plmn = models.CharField(max_length=20)
    lac = models.IntegerField()
    rac = models.IntegerField()
    tac = models.IntegerField()
    cid = models.BigIntegerField()
    freq_band = models.FloatField()
    afrn = models.FloatField()
    freq = models.FloatField()
    rsrp = models.FloatField()
    rsrq = models.FloatField()
    rscp = models.FloatField()
    ecno = models.FloatField()
    rxlev = models.FloatField()


class Test(models.Model):
    id = models.AutoField(primary_key=True)
    phone_number = models.ForeignKey(User, on_delete=models.CASCADE, to_field='phone_number')
    timestamp = models.DateTimeField()
    cell_info = models.ForeignKey(CellInfo, on_delete=models.SET_NULL, null=True)


class HTTPDownloadTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    throughput = models.FloatField()


class HTTPUploadTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    throughput = models.FloatField()


class PingTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    latency = models.FloatField()


class DNSTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    time = models.FloatField()


class WebTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    response_time = models.FloatField()


class SMSTest(models.Model):
    id = models.OneToOneField(Test, on_delete=models.CASCADE, primary_key=True)
    send_time = models.FloatField()
