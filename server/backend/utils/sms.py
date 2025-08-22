from kavenegar import *
import random

def send_otp(receptor):
    code = random.randint(100000, 999999)
    api = KavenegarAPI('50524A5671386E7538712F6373564A47742F69617852586D34586C6C4177796F45536A2F666A3432462B633D')
    params = { 'sender' : '2000660110', 
                'receptor': receptor, 
                'message' :f'Polaris sign up code : {code}'}
    
    response = ''
    response = api.sms_send(params)
    return str(code), response


send_otp('09104696634')