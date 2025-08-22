from django.contrib.auth.models import AbstractUser
from django.db import models

class User(AbstractUser):
    phone_number = models.CharField(max_length=15, unique=True, blank=False, null=False)
    is_verified = models.BooleanField(default=False)
    
    username = models.CharField(max_length=150, unique=True, blank=True, null=True)
    
    USERNAME_FIELD = 'phone_number'

class CellInfo(models.Model):
    id = models.AutoField(primary_key=True)
    phone_number = models.ForeignKey(User, on_delete=models.CASCADE, to_field='phone_number')
    lat = models.FloatField()
    lng = models.FloatField()
    timestamp = models.DateTimeField()
    gen = models.CharField(max_length=20)
    tech = models.CharField(max_length=50)
    plmn = models.CharField(max_length=20)
    cid = models.BigIntegerField()
    lac = models.IntegerField(null=True, blank=True)
    rac = models.IntegerField(null=True, blank=True)
    tac = models.IntegerField(null=True, blank=True)
    freq_band = models.FloatField(null=True, blank=True)
    afrn = models.FloatField(null=True, blank=True)
    freq = models.FloatField(null=True, blank=True)
    rsrp = models.FloatField(null=True, blank=True)
    rsrq = models.FloatField(null=True, blank=True)
    rscp = models.FloatField(null=True, blank=True)
    ecno = models.FloatField(null=True, blank=True)
    rxlev = models.FloatField(null=True, blank=True)


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
