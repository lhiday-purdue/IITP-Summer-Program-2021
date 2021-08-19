from django.shortcuts import render, redirect
import paho.mqtt.client as Mqtt
import requests
# Create your views here.
def pub(request):
    return render(request, 'pub.html')

def publish(request):
    if request.method == "POST":
        topic = '/'+request.POST['topic']
        message = request.POST['message']
        
        mqttc = Mqtt.Client('test')
        mqttc.connect("test.mosquitto.org", 1883)
        mqttc.publish(topic, message, 1)
        
        return redirect('../pub')

def httprequest(request):
    if request.method == "POST":
        response = requests.get("https://ultraman.run.goorm.io/api/value/")
        context = { 'context' : response.text}
        return render(request, 'chart.html', context)
    
def image(request):
    if request.method == "POST":
        print("it's me", request.POST.get('data'), request.POST.get('files'))
        return render(request, 'chart.html')
    else:
        return render(request, 'chart.html')
