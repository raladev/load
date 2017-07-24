import java.io.*;
import java.net.*;

public class UDPServer
{
   public static void main(String args[]) throws Exception
      {
         DatagramSocket serverSocket = new DatagramSocket(9876);       
            while(true)
               {
                  byte[] receiveData = new byte[1024];
                  byte[] sendData = new byte[1024];
                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                  serverSocket.receive(receivePacket);
                  String sentence = new String( receivePacket.getData()).trim();
                  System.out.println("REQ: " + sentence);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  String answer;
                  switch(sentence){
		  case "What is love?":	
			if (Math.random()< 0.7)
			{
				answer = "Baby don't hurt me,\ndon't hurt me no more.";
			}
			else
			{
				answer = "Yeah Yeah!";
			}
			break;
		  case "I don't know. You're not there.":				
			answer = "I give you my love, but you don't care.";
			break;
	          case "So what is right? And what is wrong?":				
			answer = "What else can I say? It's up to you.";
			break;
		  case "I don't know. What can I do?":				
			answer = "Give me your sign.";
			break;
		  case "I know we were one.":				
			answer = "just me and you.";
			break;
		  case "Don't hurt me.":				
			answer = "Don't hurt me.";
			break;
		  case "I want no other no other lover.":				
			answer = "This is our life, our time.";
			break;
		  case "We are together. Together forever.":				
			answer = "Is it love?";
			break;
	          default:
			answer = "I don't know that words (Error)";
			break;	
			} 
		  System.out.println("ANS: " + answer );
                  sendData = answer.getBytes();
                  DatagramPacket sendPacket =
                  new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
               }
      }
}