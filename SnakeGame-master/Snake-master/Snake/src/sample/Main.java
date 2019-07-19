package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main extends Application {


    // Variable
    private static int speed = 5;
    private static int width = 30;
    private static int height = 30;
    private static int foodX = 0;
    private static int foodY = 0;
    private static int tailleCorner = 25;
    private static List<Corner> snake = new ArrayList<>();
    private static Dir direction = Dir.left;
    private static boolean gameOver = false;
    private static Random rand = new Random();
    private static boolean moveSinceLastDirection = true;

    private static int score = -1;
    private static List<Integer> listScore = new ArrayList<>();

    private static AnimationTimer animationTimer;

    public void start(Stage primaryStage){
        try {
            // Création de la fenêtre
            VBox root = new VBox();
            Canvas c = new Canvas(width * tailleCorner, height * tailleCorner);
            GraphicsContext gc = c.getGraphicsContext2D();
            root.getChildren().add(c);

            listScore= BdSql.RecupScore();

            // Création d'un animationTimer pour lancer l'animation
            animationTimer = new AnimationTimer() {

                long lastTick = 0;

                public void handle(long now) {
                    if (lastTick == 0) {

                        lastTick= now ;
                        tick(gc);

                        return;


                    }
                    if(now -lastTick >1000000000/speed) {
                        lastTick = now;
                        tick(gc);
                    }
                }
            };

            animationTimer.start();


            Scene scene = new Scene(root, width* tailleCorner, height* tailleCorner);

            // Liste des contrôle avec les flèches directionnelles
            scene.addEventFilter(KeyEvent.KEY_PRESSED, key -> {
                if(moveSinceLastDirection){
                    if(key.getCode() == KeyCode.UP && direction != Dir.down) {
                        direction = Dir.up;
                        moveSinceLastDirection = false;
                    }
                    if(key.getCode() == KeyCode.LEFT && direction != Dir.right) {
                        direction = Dir.left;
                        moveSinceLastDirection = false;
                    }
                    if(key.getCode() == KeyCode.DOWN && direction != Dir.up) {
                        direction = Dir.down;
                        moveSinceLastDirection = false;
                    }
                    if(key.getCode() == KeyCode.RIGHT && direction != Dir.left) {
                        direction = Dir.right;
                        moveSinceLastDirection = false;
                    }
                }
            });



            // Création des différentes parties du serpent
            snake.add(new Corner(width/2, height/2));
            snake.add(new Corner(width/2, height/2));
            snake.add(new Corner(width/2, height/2));

            // Nouvelle nourriture
            newFood();

            // Lancement de la fenêtre
            primaryStage.setScene(scene);
            primaryStage.setTitle("SNAKE GAMEEEEEEE");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Chaque "tick" de l'animation
    private void tick(GraphicsContext gc){

        // Si le jeu est perdu
        if (gameOver){
            // On stop l'animation
            animationTimer.stop();

            // On récupère le score, on le trie et on l'affiche
            Collections.sort(listScore, Collections.reverseOrder());

            if (listScore.get(0)<score){
                listScore.set(2,listScore.get(1));
                listScore.set(1,listScore.get(0));
                listScore.set(0,score);
            }else if (listScore.get(1)<score && listScore.get(0)!= score){
                listScore.set(2,listScore.get(1));
                listScore.set(1,score);
            }else if (listScore.get(2)<score && listScore.get(1)!= score  && listScore.get(0)!= score){
                listScore.set(2,score);
            }

            // On ajoute le score en Base
            BdSql.InsertScore(listScore);

            // Affichage de l'alert en cas de Game Over pour rejouer ou quitter
            ImageIcon icon = new ImageIcon(getClass().getResource("../images/laurier.jpg"));

            int replay = JOptionPane.showConfirmDialog(null, "Meilleurs scores\n" +
                    "1er  :   "+listScore.get(0)+"\n"+
                    "2ème : "+listScore.get(1)+"\n"+
                    "3ème : "+listScore.get(2)+"\n\n"+
                    "Votre score : "+score+"\n"+
                    "Rejouer ?", "GAME OVER", JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, icon);

            // En fonction du bouton (oui/non) on lance la fonction replay() ou on quitte le jeu
            if(replay == 0){
                replay();
            }else{
                System.exit(0);
            }


            return;
        }

        // Affiche le corps du serpent
        for(int i = snake.size() - 1 ; i>=1; i--){
            snake.get(i).x = snake.get(i-1).x;
            snake.get(i).y = snake.get(i-1).y;
        }

        // Appuyer sur plusieurs touches en même temps n'affecte les déplacements
        if(!moveSinceLastDirection) {
            moveSinceLastDirection = true;
        }

        // Si le serpent touche une bordure
        switch (direction){

            case up:
                snake.get(0).y--;
                if(snake.get(0).y < 0){
                    gameOver = true;
                }

                break;
            case down:
                snake.get(0).y++;
                if(snake.get(0).y >= height){
                    gameOver = true;
                }
                break;
            case right:
                snake.get(0).x++;
                if(snake.get(0).x >= width){
                    gameOver = true;
                }
                break;
            case left:
                snake.get(0).x--;
                if(snake.get(0).x < 0){
                    gameOver = true;
                }
                break;
        }

        // A chaque fois que le serpent mange, il grandit et on affiche une nouvelle nourriture
        if(foodX == snake.get(0).x && foodY == snake.get(0).y){
            Corner pseg = snake.get(snake.size()-1);
            if (pseg.y < snake.get(snake.size()-2).y && pseg.x == snake.get(snake.size()-2).x ) {
                // Haut
                snake.add(new Corner(snake.get(snake.size()-1).x,snake.get(snake.size()-1).y-1));
            } else if (pseg.x > snake.get(snake.size()-2).x && pseg.y == snake.get(snake.size()-2).y) {
                // Droite
                snake.add(new Corner(snake.get(snake.size()-1).x+1,snake.get(snake.size()-1).y));
            } else if (pseg.y > snake.get(snake.size()-2).y && pseg.x == snake.get(snake.size()-2).x) {
                // Bas
                snake.add(new Corner(snake.get(snake.size()-1).x,snake.get(snake.size()-1).y+1));
            } else if (pseg.x < snake.get(snake.size()-2).x && pseg.y == snake.get(snake.size()-2).y) {
                // Gauche
                snake.add(new Corner(snake.get(snake.size()-1).x-1,snake.get(snake.size()-1).y));
            }
            newFood();
        }

        // Si le serpent de mange tout seul
        for(int i = 1;i<snake.size();i++){
            if(snake.get(0).x == snake.get(i).x && snake.get(0).y == snake.get(i).y){
                gameOver = true;
            }
        }

        // Images et icones
        Image imageHerbe = new Image(getClass().getResourceAsStream("../images/sand.png"));
        Image imageSnake = new Image(getClass().getResourceAsStream("../images/snake-graphics.png"));
        Image imageSnakeOmbre =new Image(getClass().getResourceAsStream("../images/snake-graphicshombre.png"));

        // Affichage des images du fond / nourriture / serpent
        Affichage(gc,imageHerbe,imageSnake,imageSnakeOmbre);

    }


    // Nouvelle nourriture et augmentation du score et de la vitesse
    private static void newFood(){

        start : while(true){
            foodX = rand.nextInt(width);
            foodY = rand.nextInt(height);

            for(Corner c: snake){
                if(c.x == foodX && c.y == foodY){
                    continue start;
                }
            }
            speed++;
            score++;
            break;
        }
    }

    // Fonction qui réhinitialise le jeu pour rejouer
    private static void replay(){
        speed = 5;
        score = -1;
        snake.clear();

        snake.add(new Corner(width/2, height/2));
        snake.add(new Corner(width/2, height/2));
        snake.add(new Corner(width/2, height/2));


        newFood();

        gameOver = false;

        animationTimer.start();
    }

    // Fonction pour l'affichage des images( serpent, fond, nourriture, ombre)
    public void Affichage(GraphicsContext gc,Image imageHerbe,Image imageSnake,Image imageSnakeOmbre){

        // Fond d'écran
        gc.drawImage(imageHerbe,0,0,width* tailleCorner,height* tailleCorner);
        GaussianBlur blur = new GaussianBlur(4);

        gc.applyEffect(blur);
        gc.setEffect(null);

        // Serpent et fonction pour     son ombre
        ombre(gc,imageSnakeOmbre);

        gc.drawImage(imageSnake,0,3*60,60,60,foodX* tailleCorner,foodY* tailleCorner, tailleCorner, tailleCorner);
        // Effets
        gc.setGlobalAlpha(0.3);
        gc.setEffect(new BoxBlur( 2,  2, 1));
        gc.setFill(Color.rgb(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
        gc.fillOval(foodX* tailleCorner +5,foodY* tailleCorner +5, tailleCorner -9, tailleCorner -9);
        gc.setGlobalAlpha(1);
        gc.setEffect(null);

        // Orientation du serpent
        for (int Compt =0;Compt<snake.size();Compt++){

            if (Compt==0){
                switch (direction){
                    case up:
                        gc.drawImage(imageSnake,3*60,0,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                        break;
                    case left:
                        gc.drawImage(imageSnake,3*60,60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                        break;
                    case down:
                        gc.drawImage(imageSnake,4*60,60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                        break;
                    case right:
                        gc.drawImage(imageSnake,4*60,0,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                        break;
                }
            }else if (Compt==(snake.size()-1)){
                Corner pseg = snake.get(Compt-1);
                if (pseg.y < snake.get(Compt).y) {
                    // Haut
                    gc.drawImage(imageSnake,3*60,2*60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.x > snake.get(Compt).x) {
                    // Droite
                    gc.drawImage(imageSnake,4*60,2*60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.y > snake.get(Compt).y) {
                    // Bas
                    gc.drawImage(imageSnake,4*60,3*60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.x < snake.get(Compt).x) {
                    // Gauche
                    gc.drawImage(imageSnake,3*60,3*60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                }

            }else {

                // Determination des angles quand le serpent tourne
                // Segment précédent
                Corner pseg = snake.get(Compt-1);
                // Segment suivant
                Corner nseg = snake.get(Compt+1);

                if (pseg.x < snake.get(Compt).x && nseg.x > snake.get(Compt).x || nseg.x < snake.get(Compt).x && pseg.x > snake.get(Compt).x) {
                    // Horizontal Gauche-Droite
                    gc.drawImage(imageSnake,60,0,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.x < snake.get(Compt).x && nseg.y > snake.get(Compt).y || nseg.x < snake.get(Compt).x && pseg.y > snake.get(Compt).y) {
                    // Angle Gauche-Bas
                    gc.drawImage(imageSnake,2*60,0,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.y < snake.get(Compt).y && nseg.y > snake.get(Compt).y || nseg.y < snake.get(Compt).y && pseg.y > snake.get(Compt).y) {
                    // Vertical Haut-Bas
                    gc.drawImage(imageSnake,2*60,60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.y < snake.get(Compt).y && nseg.x < snake.get(Compt).x || nseg.y < snake.get(Compt).y && pseg.x < snake.get(Compt).x) {
                    // Angle Haut-Gauche
                    gc.drawImage(imageSnake,2*60,2*60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.x > snake.get(Compt).x && nseg.y < snake.get(Compt).y || nseg.x > snake.get(Compt).x && pseg.y < snake.get(Compt).y) {
                    // Angle Droite-Haut
                    gc.drawImage(imageSnake,0,60,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                } else if (pseg.y > snake.get(Compt).y && nseg.x > snake.get(Compt).x || nseg.y > snake.get(Compt).y && pseg.x > snake.get(Compt).x) {
                    // Angle Bas-Droit
                    gc.drawImage(imageSnake,0,0,60,60,snake.get(Compt).x* tailleCorner,snake.get(Compt).y* tailleCorner, tailleCorner +1, tailleCorner +1);

                }

            }
        }

        // Score ombre
        gc.setEffect(null);
        gc.setGlobalAlpha(0.6);
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("",30));
        gc.fillText("Score : " + (speed - 6),12,32);
        gc.setGlobalAlpha(1.0);

        // Score
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("",30));
        gc.fillText("Score : " + (speed - 6),10,30);
    }

    // Fonction pour l'affichage de l'ombre
    public void ombre(GraphicsContext gc,Image imageSnakeOmbre){

        // Image
        gc.setEffect(null);
        gc.setGlobalAlpha(0.6);
        gc.setEffect(new BoxBlur( 2,  2, 1));
        gc.drawImage(imageSnakeOmbre,0,3*60,60,60,foodX* tailleCorner +3,foodY* tailleCorner +3, tailleCorner, tailleCorner);

        // Orientation des ombres
        for (int Compt =0;Compt<snake.size();Compt++){

            if (Compt==0){
                switch (direction){
                    case up:
                        gc.drawImage(imageSnakeOmbre,3*60,0,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                        break;
                    case left:
                        gc.drawImage(imageSnakeOmbre,3*60,60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                        break;
                    case down:
                        gc.drawImage(imageSnakeOmbre,4*60,60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                        break;
                    case right:
                        gc.drawImage(imageSnakeOmbre,4*60,0,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                        break;
                }
            }else if (Compt==(snake.size()-1)){
                Corner pseg = snake.get(Compt-1);
                if (pseg.y < snake.get(Compt).y) {
                    // Haut
                    gc.drawImage(imageSnakeOmbre,3*60,2*60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.x > snake.get(Compt).x) {
                    // Droite
                    gc.drawImage(imageSnakeOmbre,4*60,2*60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.y > snake.get(Compt).y) {
                    // Bas
                    gc.drawImage(imageSnakeOmbre,4*60,3*60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.x < snake.get(Compt).x) {
                    // Gauche
                    gc.drawImage(imageSnakeOmbre,3*60,3*60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                }
            }else {

                // Determination des angles quand le serpent tourne
                // Segment précédent
                Corner pseg = snake.get(Compt-1);
                // Segment suivant
                Corner nseg = snake.get(Compt+1);
                if (pseg.x < snake.get(Compt).x && nseg.x > snake.get(Compt).x || nseg.x < snake.get(Compt).x && pseg.x > snake.get(Compt).x) {
                    // Horizontal Gauche-Droite
                    gc.drawImage(imageSnakeOmbre,60,0,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.x < snake.get(Compt).x && nseg.y > snake.get(Compt).y || nseg.x < snake.get(Compt).x && pseg.y > snake.get(Compt).y) {
                    // Angle Gauche-Bas
                    gc.drawImage(imageSnakeOmbre,2*60,0,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.y < snake.get(Compt).y && nseg.y > snake.get(Compt).y || nseg.y < snake.get(Compt).y && pseg.y > snake.get(Compt).y) {
                    // Vertical Haut-Bas
                    gc.drawImage(imageSnakeOmbre,2*60,60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.y < snake.get(Compt).y && nseg.x < snake.get(Compt).x || nseg.y < snake.get(Compt).y && pseg.x < snake.get(Compt).x) {
                    // Angle Haut-Gauche
                    gc.drawImage(imageSnakeOmbre,2*60,2*60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.x > snake.get(Compt).x && nseg.y < snake.get(Compt).y || nseg.x > snake.get(Compt).x && pseg.y < snake.get(Compt).y) {
                    // Angle Droit-Haut
                    gc.drawImage(imageSnakeOmbre,0,60,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                } else if (pseg.y > snake.get(Compt).y && nseg.x > snake.get(Compt).x || nseg.y > snake.get(Compt).y && pseg.x > snake.get(Compt).x) {
                    // Angle Bas-Droite
                    gc.drawImage(imageSnakeOmbre,0,0,60,60,snake.get(Compt).x* tailleCorner +3,snake.get(Compt).y* tailleCorner +3, tailleCorner, tailleCorner);
                }
            }
        }

        gc.setGlobalAlpha(1);
        gc.setEffect(null);
    }

    // Lancement de l'application
    public static void main(String[] args) {
        launch(args);
    }
}
