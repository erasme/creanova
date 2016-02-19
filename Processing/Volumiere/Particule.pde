/*  
 *  class Part
 *  
 *  Chaque particule générée possède
 *  la même structure définie ci-dessous.
 *
 */

class Particule
{

	final int MAX_LIFETIME =1000*5;

	int lifetime = MAX_LIFETIME;
	float partx;        // Position x de la particule
	float party;        // Position y de la particule

	float saveX;    // Position x sauvegardée lorsqu'une attirance est déclenchée
	float saveY;    // Position y sauvegardée lorsqu'une attirance est déclenchée

	float dx;       // Décalage x de transition entre deux positions
	float dy;       // Décalage y de transition entre deux positions

	float easing = .05;                // Facteur de transition cool
	float diam;         // Diamètre de la particule
  
	color color1;  //couleur selon fréquence
	float opacite;
  
	boolean flash = false;
	float flashRadius = 0;
	float flashAlpha = 100;
  
	boolean rappel = false;
	float rappelRadius = 0;
	float rappelAlpha = 300;
 
	/////////////////////////////////////////////////////
	// Lorsqu'on génère une particule,
	// on lui fournit des paramètres uniques,
	// comme la position (aléatoire ou via le clic)
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
	// On met à jour la position de la particule
	// en considérant la translation par attirance
	void update()
	{
		lifetime = max(0, lifetime - 1);

		/////////////////////////////////////////////////////
		// Comment fonctionne la transition de la particule ?
		// 
		// Lorsqu'on clique et qu'on génère une nouvelle particule,
		// on stocke dans lastPartX et lastPartY la position de cette nouvelle particule.
		//
		// Ensuite, ici, on calcule la distance entre ce point
		// (qui est donc le point vers lequel doit se diriger chaque particule)
		// et notre point actuel.
		// La fonction dist() nous donne alors cette distance.

		float dist = dist(partx, party, lastPartX, lastPartY);

		/////////////////////////////////////////////////////
		// Ensuite, avec cette distance, on définit
		// l'attirance du point, et par extension
		// son déplacement.
		//
		// Plus les deux points sont proches,
		// plus ils sont attirés.
		//
		// Vous pouvez définir la distance maximale
		// pour attirer un point.
		//
		// Vous pouvez aussi définir la force
		// d'attirance : de 0 à 100, on augmente l'attirance.
    
		int maximumDistance = 130;
		int puissanceAttirance = 70;
    
	    float attirance = max(map(dist, 0, maximumDistance, puissanceAttirance, 0), 0);
		float closePointX = (lastPartX-saveX)*attirance/100+saveX;
		float closePointY = (lastPartY-saveY)*attirance/100+saveY;

  // Rond vert qui indique la position à atteindre par attraction
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


	void setSavePosition()
	{
		saveX =random(100,canvasWidth-100);
		saveY= random(100,canvasHeight-100);
	}

/*
void setSavePosition()
	{saveX = partx;
	saveY = party;
}*/

	void opacifier()
	{
		opacite = opacite + 0.5;
		if (opacite > 255) opacite = 255;
	}
  
	void modif()
	{
		diam = map(volume,0,100,1,10);
		color1 = color(map(frequency,200,500,100,255),100,200);
		partx = canvasWidth/2;
		party = canvasHeight/2;
	}
  
	void animFlash()
	{

		flash = true;
	}
  
	void rappel()
	{
		partx = canvasWidth/2;
		party = canvasWidth/2;
    
		rappel = true;
	}

	void draw()
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
		ellipse(partx, party, diam*random(5.5,6),diam*random(5.5,6));
	

		// rond grand translucide
		fill(color1, lopacite);                // couleur, alpha. Alpha(random) fait scintiller
		ellipse(partx, party, diam*random(28.5,28), diam*random(28.5,28));
    
		//rond moyen
		//random(230,255)

		fill(color1,lopacite*2);     
		ellipse(partx,party, diam*random(13,13.5),diam*random(13,13.5));
    
		// rond-ligne
		stroke(255,10 * opcoeff);                
		noFill();
		ellipse(partx,party, diam*17,diam*17);
		if (opacite > 40) opacite = 40;   
	}
}
