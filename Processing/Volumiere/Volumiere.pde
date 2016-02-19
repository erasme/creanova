/** picto timer **/
int tempsDelay = 30*1000;


/*
Based on an original idea from OotSideBox
Demo software written by Liliputech
*/
import processing.serial.*;

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
float easing = .05;                // Facteur de transition cool
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
float angle = 0.0;

/**Interface Son**/
import ddf.minim.*;
import ddf.minim.analysis.*;
Minim minim;
AudioInput in;
FFT fft;
int sampleRate= 44100;//sapleRate of 44100

/**Variables Particules**/
Particules particules = new Particules();
//ArrayList<Particule> Particules = new ArrayList<Particule>();       // Tableau qui contient toutes les particules
float lastPartX = 0;                   // Position x de la dernière particule ajoutée
float lastPartY = 0;                   // Position y de la dernière particule ajoutée
float lastPartDiam = 0;                // Diamètre de la dernière particule ajoutée
float lastPartColor = 0;               // Couleur de la dernière particule ajoutée
float diam2 = 0;                       // Couleur de la dernière particule ajoutée

/**Variables de Son**/
float volume = 0;
float volumePrecedent = 0;
float [] max= new float [sampleRate/2];  //array that contains the half of the sampleRate size, because FFT only reads the half of the sampleRate frequency. This array will be filled with amplitude values.
float maximum;                           //the maximum amplitude of the max array
float frequency;                         //the frequency in hertz

/***SETUP***/
void setup()
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
void draw()
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

      
  
	//La présence 
	presence = (z < 150) ? 1 : 0;

	// Variables de Volume
	volumePrecedent = volume;
	volume = in.mix.level()*100;
        
	// Relevé de Frequence
	// Calcul Frequence Max
	fft.forward(in.left);
	for (int f=0;f<sampleRate/2;f++) { //analyses the amplitude of each frequency analysed, between 0 and 22050 hertz
		max[f] = fft.getFreq(float(f)); //each index is correspondent to a frequency and contains the amplitude value
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
          
		// DURÉE SONORE //
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
	// Si inférieur ay seuil
	// 
	if (volume < seuilVolume)
	{
//		Blackout();
		e = 0;
	}
        
        
	// TIMER (INACTIVITÉ)
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
			transpaAlpha = transpaAlpha+0.2;
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

void keyPressed() 
{
//	print("\n\rKeyboard -> "+key);
//	print("\n\r");
//	myPort.write(key);
}

float map2(float val, float startMin, float startMax, float destMin, float destMax)
{
	return max(0, min(1, ((val - startMin) / (startMax - startMin)))) * (destMax - destMin) + destMin;
}

void updateColor()
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

void ApplyColors(int H, int S, int B)
{
	if (H > 360) H = 360;
	if (S > 100) S = 100;
	if (B > 100) B = 100;

//	println("color: " +("#"+hex(color(H, S, B), 6)+"\n").toLowerCase()+", B: "+B);

	colorMode(HSB, 360, 100, 100);
	serialLight.write(("#"+hex(color(H, S, B), 6)+"\n").toLowerCase());
	colorMode(RGB, 255, 255, 255);
}

void Blackout() {
//	println("Blackout");
	targetLedH = 0;
	targetLedS = 0;
	targetLedB = 0;

//	ApplyColors(0, 0, 0);
}
