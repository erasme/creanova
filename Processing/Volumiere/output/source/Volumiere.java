import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import processing.serial.*; 
import controlP5.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Volumiere extends PApplet {

/** picto timer **/
int tempsDelay = 30*1000;


/*
Based on an original idea from OotSideBox
Demo software written by Liliputech
*/


/*int Blue=0;
 int Green=0;
 int Red=0;
 */

int aigueR = 150;
int aigueV = 0;
int aigueB = 60;

int graveR = 60;
int graveV = 0;
int graveB = 150;

int ledH = 0; // entre 0 et 360
int ledS = 100; // entre 0 et 100
int ledB = 0; // entre 0 et 100

int targetLedH = 237; // entre 0 et 360
int targetLedS = 100; // entre 0 et 100
int targetLedB = 100; // entre 0 et 100

int e=5;

int presence = 0;
Serial serialLight;

/**Taille de la scene**/
int canvasWidth = 800;     // Largeur du canvas
int canvasHeight = 800;    // Hauteur du canvas

/**Rond noir**/
PImage rond;

/**Animation**/
float easing = .05f;                // Facteur de transition cool
int seuilVolume = 15;

/** Timer **/
int temps2;
int temps1;
boolean descendant;
boolean montant;
int rappelRadius=0;
int rappelAlpha=255;
int parleAlpha=100;
int parleAlpha2=0;
float transpaAlpha=0;
PImage picto;

/** Texte de rappel **/
PFont f;
PFont font;
float angle = 0.0f;

/**Interface Son**/


Minim minim;
AudioInput in;
FFT fft;
int sampleRate= 44100;//sapleRate of 44100

/**Variables Particules**/
Particules particules = new Particules();
//ArrayList<Particule> Particules = new ArrayList<Particule>();       // Tableau qui contient toutes les particules
float lastPartX = 0;                   // Position x de la derni\u00e8re particule ajout\u00e9e
float lastPartY = 0;                   // Position y de la derni\u00e8re particule ajout\u00e9e
float lastPartDiam = 0;                // Diam\u00e8tre de la derni\u00e8re particule ajout\u00e9e
float lastPartColor = 0;               // Couleur de la derni\u00e8re particule ajout\u00e9e
float diam2 = 0;                       // Couleur de la derni\u00e8re particule ajout\u00e9e

/**Variables de Son**/
float volume = 0;
float volumePrecedent = 0;
float [] max= new float [sampleRate/2];  //array that contains the half of the sampleRate size, because FFT only reads the half of the sampleRate frequency. This array will be filled with amplitude values.
float maximum;                           //the maximum amplitude of the max array
float frequency;                         //the frequency in hertz

/***SETUP***/
public void setup()
{
	//Rendu
	size(displayWidth, displayHeight, OPENGL);

	// hide mouse cursor
	noCursor();

	//3DPad
//	if (frame != null) frame.setResizable(true);
//	setupControl();
        //x = dynX;
        //y = dynY;        
		z = dynZ;
        
	canvasWidth = displayWidth;
	canvasHeight = displayHeight;  
//	frameRate(60);
//  smooth();
	noSmooth();

	// Rond noir
	rond = loadImage("ellipseGrande.png");

	// Timer
	temps1 = 0;
	temps2 = 0;
	descendant = false;
	montant = false;
	picto = loadImage("pictoMoyenne.png");
	picto.resize(200, 200);

	// son
	minim = new Minim(this);
	//minim.debugOn();
	in = minim.getLineIn(Minim.MONO, 4096, sampleRate);
	fft = new FFT(in.left.size(), sampleRate);
  
	println(Serial.list());
	
	myPort = new Serial(this, "/dev/serial/by-path/pci-0000:00:14.0-usb-0:3:1.0", 115200);
	myPort.stop();
	myPort = new Serial(this, "/dev/serial/by-path/pci-0000:00:14.0-usb-0:3:1.0", 115200);
	myPort.clear();
  
	// setup the arduino for lights
	serialLight = new Serial(this, "/dev/serial/by-path/pci-0000:00:14.0-usb-0:4:1.0", 9600);
	serialLight.clear();

//	Blackout();	

	// Particules = (Part[]) append(Particules, new Part(Particules.length, lastPartX, lastPartY, lastPartDiam));
	particules.add(new Particule(lastPartX, lastPartY, lastPartDiam));
} 

/***BOUCLE DRAW***/
public void draw()
{
  
//x = dynX;
//y = dynY;
z = dynZ;
        
//  println(z+" = Z du 3Dpad");
//	println(frameRate);

	//Rafraichir l'affichage
	background(0);

	// f = createFont("Helvetica", 40, true);
	// textFont(f);
	// textAlign(CENTER);

	// LEDs
	//noStroke();
	//Blackout();
        
	//Affichage des logs
	//println("x = "+x);
	//println("y = "+y);
    // Affichage indications textuelles
    //textSize(50); 
    //text (frequency-6+"(freq)", 150, 80);
    //text (volume+"(vol)", 500, 80);
    //fill(0, 255, 0);
    //text (temps1+"(temps1)", 150, 120);
    //text (temps2+"(temps2)", 500, 120);

	// Rond noir
	rond = loadImage("ellipseGrande.png");

      
  
	//La pr\u00e9sence 
	presence = (z < 150) ? 1 : 0;

	// Variables de Volume
	volumePrecedent = volume;
	volume = in.mix.level()*100;
        
	// Relev\u00e9 de Frequence
	// Calcul Frequence Max
	fft.forward(in.left);
	for (int f=0;f<sampleRate/2;f++) { //analyses the amplitude of each frequency analysed, between 0 and 22050 hertz
		max[f] = fft.getFreq(PApplet.parseFloat(f)); //each index is correspondent to a frequency and contains the amplitude value
	}
	maximum=max(max);//get the maximum value of the max array in order to find the peak of volume
	for (int i = 0; i < max.length; i++) {// read each frequency in order to compare with the peak of volume
		if (max[i] == maximum) {//if the value is equal to the amplitude of the peak, get the index of the array, which corresponds to the frequency
			frequency= i;
		}
	}
     
	//AFFICHAGE PRINCIPAL
	if (presence > 0)
	{
  
                // Curseur du capteur 3DPAD
                //fill(255,60);
                //noStroke();
                //ellipse(x,y,10,10);
                //fill(255,30);
                //ellipse(x,y,random(35,35.5),random(35,35.5));
        
        
		// if present lightup
		targetLedB = 100;
		

		// FRONT MONTANT //
		if (volume >= seuilVolume && volumePrecedent < seuilVolume)
		{
			lastPartX = canvasWidth/2;
			lastPartY = canvasHeight/2;
			lastPartDiam = map(volume, 0, 100, 1, 10);
                  
			particules.add(new Particule(lastPartX, lastPartY, lastPartDiam));
	
			// faire disparaitre le "timer"
			rappelRadius=0;
			rappelAlpha=255;
			parleAlpha=100;
			parleAlpha2=0;
			transpaAlpha=0;
		}
          
		// DUR\u00c9E SONORE //
		if (volume >= seuilVolume && volumePrecedent >= seuilVolume)
		{
			int a;
			int b;
			int c;
            
			int mixR = (int)map(frequency, 300, 800, graveR, aigueR);
			int mixV = (int)map(frequency, 300, 800, graveV, aigueV);
			int mixB = (int)map(frequency, 300, 800, graveB, aigueB);
      
			particules.get(particules.size()-1).opacifier();
			particules.get(particules.size()-1).modif();
              
			e += 1;
			mixR += e;
			mixV += e;
			mixB += e;
			if (mixR>255||mixV>255||mixB>255) {
				mixR-=e;
				mixV-=e;
				mixB-=e;
			}
			fill(mixR,mixV,mixB);

//			println("SET COLOR");
//			println("freq: "+map2(frequency, 200, 2000, 0, 360));
			targetLedH = (int)map2(frequency, 200, 1000, 237, 360);
			targetLedS = 100;
//			targetLedB = 100;
//			ApplyColors((int)map2(frequency, 200, 2000, 0, 360), 100, 100);		
		}
          
		// FRONT DESCENDANT //
		if (volume < seuilVolume && volumePrecedent >= seuilVolume)
		{

			particules.get(particules.size()-1).animFlash();
			particules.get(particules.size()-1).setSavePosition();
			descendant = true;
			temps1 = millis();
		}
	}
	else {
		// if present light down
		targetLedB = 0;
		targetLedS = 0;
	}
      
	/////////////////////////////////////////////////////
	// Si inf\u00e9rieur ay seuil
	// 
	if (volume < seuilVolume)
	{
//		Blackout();
		e = 0;
	}
        
        
	// TIMER (INACTIVIT\u00c9)
	if (descendant = true)
	{
		temps2 = millis()-temps1;
	}

	// animation du "timer"      
	if (temps2 > tempsDelay)
	{
		noStroke();
		fill(255,rappelAlpha);
		ellipse(canvasWidth/2,canvasHeight/2,rappelRadius,rappelRadius);
		ellipse(canvasWidth/2,canvasHeight/2,rappelRadius+50,rappelRadius+50);
		ellipse(canvasWidth/2,canvasHeight/2,rappelRadius+100,rappelRadius+100);
            
		fill(255,transpaAlpha);
		ellipse(canvasWidth/2,canvasHeight/2,random(400,403),random(400,403));
		fill(255,transpaAlpha);
		ellipse(canvasWidth/2,canvasHeight/2,random(500,503),random(500,503));
		stroke(255,transpaAlpha);  
		strokeWeight(2);      
		fill(0,0);
		ellipse(canvasWidth/2,canvasHeight/2,random(350,353),random(350,353));
		// fill(0,parleAlpha);
		// text ("Parle moi", canvasWidth/2,canvasHeight/2);
		tint(255, parleAlpha2);
		image(picto,canvasWidth/2-100,canvasHeight/2-100);
      
		noFill();
            
		if (parleAlpha<800)
		{
			rappelRadius = rappelRadius+10;
			rappelAlpha = rappelAlpha-5;
			parleAlpha = parleAlpha+5;
			parleAlpha2 = parleAlpha2+8;
			transpaAlpha = transpaAlpha+0.2f;
		}
	}
    
	// MISE A JOUR DES PARTICULE
	particules.update();
	particules.draw();

	updateColor();



	// Marsque rond et noi
	tint(0);
	//image(rond, 0, 0, canvasWidth, canvasHeight);
}

public void keyPressed() 
{
//	print("\n\rKeyboard -> "+key);
//	print("\n\r");
//	myPort.write(key);
}

public float map2(float val, float startMin, float startMax, float destMin, float destMax)
{
	return max(0, min(1, ((val - startMin) / (startMax - startMin)))) * (destMax - destMin) + destMin;
}

public void updateColor()
{
//	println("updateColor ledB: "+ledB+", targetB: "+targetLedB);
	if(targetLedH > ledH)
		ledH += 4;
	else if(targetLedH < ledH)
		ledH -= 4;
	if(targetLedS > ledS)
		ledS += 4;
	else if(targetLedS < ledS)
		ledS -= 4;
	if(targetLedB > ledB)
		ledB += 8;
	else if(targetLedB < ledB)
		ledB -= 1;

	ApplyColors(ledH, ledS, ledB);
}

public void ApplyColors(int H, int S, int B)
{
	if (H > 360) H = 360;
	if (S > 100) S = 100;
	if (B > 100) B = 100;

//	println("color: " +("#"+hex(color(H, S, B), 6)+"\n").toLowerCase()+", B: "+B);

	colorMode(HSB, 360, 100, 100);
	serialLight.write(("#"+hex(color(H, S, B), 6)+"\n").toLowerCase());
	colorMode(RGB, 255, 255, 255);
}

public void Blackout() {
//	println("Blackout");
	targetLedH = 0;
	targetLedS = 0;
	targetLedB = 0;

//	ApplyColors(0, 0, 0);
}
/*
Based on an original idea from OotSideBox
Demo software written by Liliputech
*/



ControlP5 cp5;
DropdownList dSerial;
Button bCalibrate, bSetup;

public void setupControl() {
  cp5 = new ControlP5(this);

  bCalibrate = cp5.addButton("Calibrate")
                  .setValue(0)
                  .setPosition(120,10)
                  .setColorBackground(color(60))
                  .setColorActive(color(255, 128));

  bSetup = cp5.addButton("Setup")
                  .setValue(0)
                  .setPosition(120,30)
  		  .setColorBackground(color(60))
                  .setColorActive(color(255, 128));

  dSerial = cp5.addDropdownList("Serial-List").setPosition(10, 20);
  dSerial.captionLabel().set("Connect");
  for (int i=0; i<Serial.list ().length; i++) {
    dSerial.addItem(Serial.list()[i], i);
  }
  dSerial.setColorBackground(color(60));
  dSerial.setColorActive(color(255, 128));
 
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.isGroup()) {

      if (portIndex!= -1) myPort.stop();
      portIndex = PApplet.parseInt(dSerial.getValue());
    
      myPort = new Serial(this, Serial.list()[portIndex], 115200);
      myPort.clear();
      println("3Dpad connecting to -> " + Serial.list()[portIndex]);
    
  }
}

public void Connect(int portIndex) {
      if (portIndex!= -1) myPort.stop();
    
      myPort = new Serial(this, Serial.list()[portIndex], 115200);
      myPort.clear();
      println("3Dpad connecting to -> " + Serial.list()[portIndex]);
}

public void Calibrate(int value) {
	myPort.write('A');
}

public void Setup(int value) {
	myPort.write('S');
}
/*
Based on an original idea from OotSideBox
Demo software written by Liliputech
*/

int x, y;
int z=300;
int dynX, dynY, dynZ;
int turnL, turnR;

public void calculateCoord() {
           
      int rawx = PApplet.parseInt(rawValues.substring(20, 24));
      int rawy = PApplet.parseInt(rawValues.substring(25, 29));
      int rawz = PApplet.parseInt(rawValues.substring(30, 34));
      int rawDynX = PApplet.parseInt(rawValues.substring(35, 39));
      int rawDynY = PApplet.parseInt(rawValues.substring(40, 44));
      int rawDynZ = PApplet.parseInt(rawValues.substring(45, 49));
      
      //x = rawx*width/100;
      //y = (100-rawy)*height/100;
      z = rawz;
      
      dynX = rawDynX*width/100;
      dynY = (100-rawDynY)*height/100;
      dynZ = rawDynZ;
      
      turnL = PApplet.parseInt(rawValues.substring(50, 54));
      turnR = PApplet.parseInt(rawValues.substring(55, 59));
}

class Filter
{
//	float[]
}
/*  
 *  class Part
 *  
 *  Chaque particule g\u00e9n\u00e9r\u00e9e poss\u00e8de
 *  la m\u00eame structure d\u00e9finie ci-dessous.
 *
 */

class Particule
{

	final int MAX_LIFETIME =1000*5;

	int lifetime = MAX_LIFETIME;
	float partx;        // Position x de la particule
	float party;        // Position y de la particule

	float saveX;    // Position x sauvegard\u00e9e lorsqu'une attirance est d\u00e9clench\u00e9e
	float saveY;    // Position y sauvegard\u00e9e lorsqu'une attirance est d\u00e9clench\u00e9e

	float dx;       // D\u00e9calage x de transition entre deux positions
	float dy;       // D\u00e9calage y de transition entre deux positions

	float easing = .05f;                // Facteur de transition cool
	float diam;         // Diam\u00e8tre de la particule
  
	int color1;  //couleur selon fr\u00e9quence
	float opacite;
  
	boolean flash = false;
	float flashRadius = 0;
	float flashAlpha = 100;
  
	boolean rappel = false;
	float rappelRadius = 0;
	float rappelAlpha = 300;
 
	/////////////////////////////////////////////////////
	// Lorsqu'on g\u00e9n\u00e8re une particule,
	// on lui fournit des param\u00e8tres uniques,
	// comme la position (al\u00e9atoire ou via le clic)
	// ou l'id

	Particule (float input_x, float input_y, float input_diam)
	{
		diam = input_diam;
		partx = input_x;
		party = input_y;
		saveX = input_x;
		saveY = input_y;
		color1 = color(map(frequency,200,500,100,255),100,200);
		opacite = 5;
	}

	/////////////////////////////////////////////////////
	// On met \u00e0 jour la position de la particule
	// en consid\u00e9rant la translation par attirance
	public void update()
	{
		lifetime = max(0, lifetime - 1);

		/////////////////////////////////////////////////////
		// Comment fonctionne la transition de la particule ?
		// 
		// Lorsqu'on clique et qu'on g\u00e9n\u00e8re une nouvelle particule,
		// on stocke dans lastPartX et lastPartY la position de cette nouvelle particule.
		//
		// Ensuite, ici, on calcule la distance entre ce point
		// (qui est donc le point vers lequel doit se diriger chaque particule)
		// et notre point actuel.
		// La fonction dist() nous donne alors cette distance.

		float dist = dist(partx, party, lastPartX, lastPartY);

		/////////////////////////////////////////////////////
		// Ensuite, avec cette distance, on d\u00e9finit
		// l'attirance du point, et par extension
		// son d\u00e9placement.
		//
		// Plus les deux points sont proches,
		// plus ils sont attir\u00e9s.
		//
		// Vous pouvez d\u00e9finir la distance maximale
		// pour attirer un point.
		//
		// Vous pouvez aussi d\u00e9finir la force
		// d'attirance : de 0 \u00e0 100, on augmente l'attirance.
    
		int maximumDistance = 130;
		int puissanceAttirance = 70;
    
	    float attirance = max(map(dist, 0, maximumDistance, puissanceAttirance, 0), 0);
		float closePointX = (lastPartX-saveX)*attirance/100+saveX;
		float closePointY = (lastPartY-saveY)*attirance/100+saveY;

  // Rond vert qui indique la position \u00e0 atteindre par attraction
 //noStroke();
//fill(0, 255, 0);
 //ellipse(closePointX, closePointY, 3, 3);

		dx = closePointX - partx;
		dy = closePointY - party;

		if (abs(dx) > 1) {
			partx += dx * easing;
		}
		if (abs(dy) > 1) {
			party += dy * easing;
		}
	}


	public void setSavePosition()
	{
		saveX =random(100,canvasWidth-100);
		saveY= random(100,canvasHeight-100);
	}

/*
void setSavePosition()
	{saveX = partx;
	saveY = party;
}*/

	public void opacifier()
	{
		opacite = opacite + 0.5f;
		if (opacite > 255) opacite = 255;
	}
  
	public void modif()
	{
		diam = map(volume,0,100,1,10);
		color1 = color(map(frequency,200,500,100,255),100,200);
		partx = canvasWidth/2;
		party = canvasHeight/2;
	}
  
	public void animFlash()
	{

		flash = true;
	}
  
	public void rappel()
	{
		partx = canvasWidth/2;
		party = canvasWidth/2;
    
		rappel = true;
	}

	public void draw()
	{

		float opcoeff = pow((float)lifetime / (float)MAX_LIFETIME, 2);
		float lopacite = opacite * opcoeff;


		noStroke();

		//invisible
		if(flash) {
			flashRadius = flashRadius+8;
			flashAlpha = flashAlpha-8;
			fill(255,flashAlpha);
			ellipse(partx,party,flashRadius,flashRadius);
      
			if(flashRadius > 400) {
				flash = false; 
			}
		}

		// rond coeur
		fill(color1, lopacite*15);            
		ellipse(partx, party, diam*2,diam*2);
	

    	// rond coeur
		fill(color1, lopacite*10);            
		ellipse(partx, party, diam*random(5.5f,6),diam*random(5.5f,6));
	

		// rond grand translucide
		fill(color1, lopacite);                // couleur, alpha. Alpha(random) fait scintiller
		ellipse(partx, party, diam*random(28.5f,28), diam*random(28.5f,28));
    
		//rond moyen
		//random(230,255)

		fill(color1,lopacite*2);     
		ellipse(partx,party, diam*random(13,13.5f),diam*random(13,13.5f));
    
		// rond-ligne
		stroke(255,10 * opcoeff);                
		noFill();
		ellipse(partx,party, diam*17,diam*17);
		if (opacite > 40) opacite = 40;   
	}
}

class Particules
{
	ArrayList<Particule> particules = new ArrayList<Particule>();

	Particules()
	{
	}

	public void add(Particule particule) 
	{
		particules.add(particule);
	}

	public Particule get(int pos)
	{
		return particules.get(pos);
	}

	public int size()
	{
		return particules.size();
	}

	public void update()
	{
		ArrayList<Particule> removeList = new ArrayList<Particule>();
		for(Particule p: particules) {
			p.update();
			if(p.lifetime == 0) {
				removeList.add(p);
			}
		}
		for(Particule p: removeList) {
			particules.remove(p);
		}
	}

	public void draw()
	{
		for(Particule p: particules) {
			p.draw();
		}
	}
}
/*
Based on an original idea from OotSideBox
Demo software written by Liliputech
*/



Serial myPort;
int portIndex=-1;
int counterTrame = 0;
int automateReceive = 0;
private String receivedString;
String rawValues;
String detectedMotion;


public void serialEvent(Serial p)
{
  if (myPort.available() > 0)
  {
    char inByte = myPort.readChar();
    processSerial(inByte);
  }
}

public void processSerial(char chartmp) {
  
  if( chartmp == '.') print(".");
 
  switch(automateReceive) {
  case 0:
    if (chartmp == '>') automateReceive = 1; 
    break;
    
  case 1:
    if (chartmp == 'A') automateReceive = 2; 
    if (chartmp == 'G') automateReceive = 3; 
    if (chartmp == 'V') {
      automateReceive = 4;
      counterTrame = 0;
      receivedString = "";
    }
  break;
    
   case 2: //etats de l'automate 3Dpad
     print("\n\rState: "); 
     
     switch(chartmp)
     {
           case '0':
           case '1':
                     print("Autocalibration, please wait ...");
                     break;
           case '2':
                     print("Setup ");
                     break;
           case '3':
                     print("Run");
                     break;
     }
     myPort.write('V');   
     automateReceive = 0;
   break;
    
  case 3: //gestures
  //   print("\n\rGest: "); 
     detectGesture(chartmp);
     automateReceive = 0;
     print(detectedMotion);    
     
  break;
  case 4:
    receivedString += chartmp;
    counterTrame++;
    if (counterTrame == 60) {
      rawValues=receivedString;
      calculateCoord();
      
      //println(rawValues);
      automateReceive = 0;
    }
    break;
  }
}

public void detectGesture(char chartmp) {
  switch(chartmp) {
  case '0':
    detectedMotion = "OUT";
    break;
  case '1':
    detectedMotion = "IN";
    break;
  case '2':
    detectedMotion = "RIGHT";
    break;
  case '3':
    detectedMotion = "LEFT";
    break;
  case '4':
    detectedMotion = "UP";
    break;
  case '5':
    detectedMotion = "DOWN";
    break;
  case '6':
    detectedMotion = "TurnL : "+ turnL;
    break;
  case '7':
    detectedMotion = "TurnR : " + turnR;
    break;
  case '8':
    detectedMotion = "PUSH";
    break;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Volumiere" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
