from django.shortcuts import render
from django.contrib.auth import authenticate, login, logout



def home(request):
    return render(request, 'home.html', {})


def login(rquest):
    return render(request, 'login.html', {})

def logout(rquest):
    return render(request, 'logout.html', {})

