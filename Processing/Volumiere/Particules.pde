
class Particules
{
	ArrayList<Particule> particules = new ArrayList<Particule>();

	Particules()
	{
	}

	void add(Particule particule) 
	{
		particules.add(particule);
	}

	Particule get(int pos)
	{
		return particules.get(pos);
	}

	int size()
	{
		return particules.size();
	}

	void update()
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

	void draw()
	{
		for(Particule p: particules) {
			p.draw();
		}
	}
}
