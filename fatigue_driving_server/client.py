#coding:utf-8

from socket import *

print("=====================UDP客户端=====================")

HOST = '127.0.0.1'   #本机测试
PORT = 9999 #端口号
BUFSIZ = 1024 #接收消息的缓冲大小
ADDR = (HOST, PORT)

udpCliSock = socket(AF_INET, SOCK_DGRAM) #创建客户端套接字

while True:
  data = input('> ') #接收用户输入
  if not data:   #如果用户输入为空，直接回车就会发送""，""就是代表false
      break
  udpCliSock.sendto(bytes(data,'utf-8'), ADDR) #客户端发送消息，必须发送字节数组
  data, ADDR = udpCliSock.recvfrom(BUFSIZ) #接收回应消息，接收到的是字节数组
  if not data:   #如果接收服务器信息失败，或没有消息回应
      break
  print(str(data,'utf-8')) #打印回应消息

udpCliSock.close()#关闭客户端socket