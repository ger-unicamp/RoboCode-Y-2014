package fibo;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.*;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

//Método de Circular Targeting : http://robowiki.net/wiki/Circular_Targeting/Walkthrough

/**
 * Fibonnaci - a robot by Henrique Amitay
 */

public class Fibonnaci extends AdvancedRobot
{
	
	boolean andandoParaFrente; //True se o robo estiver andando para a frente
	boolean pertoDoMuro; //True se o robo estiver perto de um muro	
	public void run() {
		// Inicialização:
		// As partes do robo devem se mover independentemente
        setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		//Cores do Robo
		setBodyColor(Color.black);
		setGunColor(Color.orange);
		setRadarColor(Color.red);
		setScanColor(Color.red);
		setBulletColor(Color.yellow);
		//Primeiramente, checamos se o robo esta perto de um muro
		if(getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50){
		pertoDoMuro = true;
		} else {
		pertoDoMuro = false;
		}
		
		setAhead(40000);//O robo anda para a frente até ser comandado a fazer diferente
		andandoParaFrente = true;//Afinal o robo andou para a frente

		// Metodos iniciais para busca do robo inimigo
		do {
		//Novamente, checamos se estamos perto do muro
		if (getX() > 50 && getY() > 50 && getBattleFieldWidth() - getX() > 50 && getBattleFieldHeight() - getY() > 50 && pertoDoMuro == true) {
				pertoDoMuro = false;
			}
			if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50 ) {
				if ( pertoDoMuro == false){
					meiaVolta();
					pertoDoMuro = true;
				}
			}
		//O radar rotaciona 360º infinitamente até achar o robo inimigo
		if ( getRadarTurnRemaining() == 0.0)
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
		execute();
			} while(true);
}//run


	/**
	 * meiaVolta: Altera o sentido de translação
	 */
		public void meiaVolta() {
		if (andandoParaFrente) {
			setBack(40000);
			andandoParaFrente = false;
		} else {
			setAhead(40000);
			andandoParaFrente = true;
		}
	}//meiaVolta


	/**
	 * onScannedRobot: O que fazer quando achar um robo
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		//Rotina do Radar ->Circular Targeting
		double angleToEnemy = getHeadingRadians() + e.getBearingRadians(); //Retorna o angulo do oponente em relação ao eixo de referencias
		double radarTurn = Utils.normalRelativeAngle (angleToEnemy - getRadarHeadingRadians());
		double extraTurn = Math.min( Math.atan( 36.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);
		setTurnRadarRightRadians(radarTurn);
		
		 //Dimensões do Campo de Batalha
		 double battleFieldHeight = getBattleFieldHeight(), battleFieldWidth = getBattleFieldWidth();
		 double maxDistance = Math.sqrt((battleFieldHeight*battleFieldHeight)+(battleFieldWidth*battleFieldWidth));
		 //Posição do nosso robô
		 double myX = getX();
		 double myY = getY();		 

		 //Dados e Localização Relativa do inimigo
		 double eX = getX() + Math.sin(angleToEnemy) + e.getDistance();
		 double eY = getY() + Math.cos(angleToEnemy) + e.getDistance();
		 double eHeading = e.getHeadingRadians();
		 double eHeadingOld = eHeading;
		 double eHeadingChange = eHeading - eHeadingOld;
		 double eVelocity = e.getVelocity();
		 
		//Potencia do Tiro
		double power = bulletPower (e.getDistance(),maxDistance,e.getEnergy());
		
		//Rotação da Arma
		double delta = 0;
		double predictedX = eX, predictedY = eY;
		
		while( (++delta) * (20.0 - 3.0*power) < Point2D.Double.distance(myX, myY, predictedX, predictedY))
		{
			predictedX += Math.sin(eHeading)*eVelocity;
			predictedY += Math.cos(eHeading)*eVelocity;
			eHeading += eHeadingChange;
			if( predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0)
			{
				predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);	
				predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
				break;
			}
		}
		 
		setTurnGunRightRadians(Utils.normalRelativeAngle(angleToEnemy - getGunHeadingRadians()));
		fire(power);
		 //Rotina de Translação
		 if (andandoParaFrente){
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 80));
		 } else {
			setTurnRight(Utils.normalRelativeAngleDegrees(e.getBearing() + 100));
		 }
	 }

	/**
	 * onHitByBullet: Quando atingido
	 */
	public void onHitRobot(HitRobotEvent e) {
		// Talvez alterar o sentido de rotação quando for atingido?
		if(e.isMyFault()){
		meiaVolta();
	}}
	
	/**
	 * onHitWall: Quando atingir um muro
	 */
	public void onHitWall(HitWallEvent e) {
		// Quando atingirmos o muro, daremos meia volta
		meiaVolta();
	}	
	
	double bulletPower(double distance, double max, double energy){
		if(distance < 65) return 3;
		return Math.min(3, (3*(energy/100)*(1-(distance/max))) - ((3*(energy/100)*(1-(distance/max)))%0.1));
		}//bulletPower

}




