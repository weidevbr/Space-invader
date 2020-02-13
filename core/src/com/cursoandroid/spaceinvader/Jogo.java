package com.cursoandroid.spaceinvader;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture[] ets;
    private Texture fundo;
    private Texture rochaBaixo;
    private Texture rochaTopo;
    private Texture gameOver;


    private ShapeRenderer shapeRenderer;
    private Circle circuloEt;
    private Rectangle retanguloRochaCima;
    private Rectangle retanguloRochaBaixo;


    private float larguraDispositivo;
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade=2;
    private float posicaoInicialVerticalEt=0;
    private float posicaoRochaHorizontal;
    private float posicaoRochaVertical;
    private float espacoEntreRochas;
    private Random random;
    private int pontos=0;
    private int pontuacaoMaxima=0;
    private boolean passouRocha=false;
    private int estadoJogo = 0;
    private float posicaoHorizontalEt = 0;

    //Exibiçao de textos
    BitmapFont textoPontuacao;
    BitmapFont textoReiniciar;
    BitmapFont textoMelhorPontuacao;

    //Configuração dos sons
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    //Objeto salvar pontuacao
    Preferences preferencias;

    //Objetos para câmera
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGHT = 1280;

    @Override
    public void create () {
        inicializarTexturas();
        inicializaObjetos();
    }

    @Override
    public void render () {

        // Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

        verificarEstadoJogo();
        validarPontos();
        desenharTexturas();
        detectarColisoes();

    }


    private void verificarEstadoJogo(){

        boolean toqueTela = Gdx.input.justTouched();


        if( estadoJogo == 0 ){

            /* Aplica evento de toque na tela */
            if( toqueTela ){
                gravidade = -15;
                estadoJogo = 1;
                somVoando.play();
            }

        }else if( estadoJogo == 1 ){

            /* Aplica evento de toque na tela */
            if( toqueTela ){
                gravidade = -15;
                somVoando.play();
            }

            /*Movimentar o cano*/
            posicaoRochaHorizontal -= Gdx.graphics.getDeltaTime() * 200;
            if( posicaoRochaHorizontal < -rochaTopo.getWidth() ){
                posicaoRochaHorizontal = larguraDispositivo;
                posicaoRochaVertical = random.nextInt(400) - 200;
                passouRocha = false;
            }


            if( posicaoInicialVerticalEt > 0 || toqueTela )
                posicaoInicialVerticalEt = posicaoInicialVerticalEt - gravidade;

            gravidade++;

        }else if( estadoJogo == 2 ){

            if( pontos > pontuacaoMaxima ){
                pontuacaoMaxima = pontos;
                preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
                preferencias.flush();
            }

            posicaoHorizontalEt -= Gdx.graphics.getDeltaTime()*500;

            /* Aplica evento de toque na tela */
            if( toqueTela ){
                estadoJogo = 0;
                passouRocha = false;
                pontos = 0;
                gravidade = 0;
                posicaoHorizontalEt = 0;
                posicaoInicialVerticalEt = alturaDispositivo / 2;
                posicaoRochaHorizontal = larguraDispositivo;
            }

        }

    }

    private void detectarColisoes(){

        circuloEt.set(
                50 + posicaoHorizontalEt + ets[0].getWidth() / 2 ,posicaoInicialVerticalEt + ets[0].getHeight()/2,ets[0].getWidth()/2
        );

        retanguloRochaBaixo.set(
                posicaoRochaHorizontal, alturaDispositivo / 2 - rochaBaixo.getHeight() - espacoEntreRochas/2 + posicaoRochaVertical,
                rochaBaixo.getWidth(), rochaBaixo.getHeight()
        );
        retanguloRochaCima.set(
                posicaoRochaHorizontal,alturaDispositivo / 2 + espacoEntreRochas / 2 + posicaoRochaVertical,
                rochaTopo.getWidth(), rochaTopo.getHeight()
        );

        boolean colidiuCanoCima = Intersector.overlaps(circuloEt, retanguloRochaCima);
        boolean colidiuCanoBaixo = Intersector.overlaps(circuloEt, retanguloRochaBaixo);

        if( colidiuCanoCima || colidiuCanoBaixo || posicaoInicialVerticalEt<=0 || posicaoInicialVerticalEt>alturaDispositivo ){
            if( estadoJogo == 1 ){
                somColisao.play();
                estadoJogo = 2;
            }

        }
    }

    private void desenharTexturas(){

        batch.setProjectionMatrix( camera.combined );

        batch.begin();

        batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
        batch.draw( ets[ (int) variacao] ,50 + posicaoHorizontalEt,posicaoInicialVerticalEt);
        batch.draw(rochaBaixo, posicaoRochaHorizontal, alturaDispositivo / 2 - rochaBaixo.getHeight() - espacoEntreRochas/2 + posicaoRochaVertical);
        batch.draw(rochaTopo, posicaoRochaHorizontal,alturaDispositivo / 2 + espacoEntreRochas / 2 + posicaoRochaVertical );
        textoPontuacao.draw(batch, String.valueOf(pontos),larguraDispositivo/2, alturaDispositivo -110 );

        if( estadoJogo == 2 ){
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2, alturaDispositivo / 2 );
            textoReiniciar.draw(batch, "Touch to restart!", larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight()/2 );
            textoMelhorPontuacao.draw(batch, "Your score: "+ pontuacaoMaxima +" points", larguraDispositivo/2 -140,alturaDispositivo/2 - gameOver.getHeight());
        }

        if(estadoJogo == 0){

            textoPontuacao.draw(batch,"Touch!",0, alturaDispositivo/3);
        }

        batch.end();

    }

    public void validarPontos(){

        if( posicaoRochaHorizontal < 200-ets[0].getWidth() ){
            if(!passouRocha){
                pontos++;
                passouRocha = true;
                somPontuacao.play();
            }
        }

        variacao += Gdx.graphics.getDeltaTime() * 10;
        if (variacao > 3 )
            variacao = 0;

    }

    private void inicializarTexturas(){
        ets = new Texture[3];
        ets[0] = new Texture("et1.png");
        ets[1] = new Texture("et2.png");
        ets[2] = new Texture("et3.png");

        fundo = new Texture("fundo.png");
        rochaBaixo = new Texture("rocha_baixo_maior.png");
        rochaTopo = new Texture("rocha_topo_maior.png");
        gameOver = new Texture("game_over.png");

    }

    private void inicializaObjetos(){

        batch = new SpriteBatch();
        random = new Random();

        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVerticalEt = alturaDispositivo / 2;
        posicaoRochaHorizontal = larguraDispositivo;
        espacoEntreRochas = 260;

        //Configurações dos textos
        textoPontuacao = new BitmapFont();
        textoPontuacao.setColor(Color.WHITE);
        textoPontuacao.getData().setScale(10);

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuacao = new BitmapFont();
        textoMelhorPontuacao.setColor(Color.RED);
        textoMelhorPontuacao.getData().setScale(2);

        //Formas Geeométricas para colisoes;
        shapeRenderer = new ShapeRenderer();
        circuloEt = new Circle();
        retanguloRochaBaixo = new Rectangle();
        retanguloRochaCima = new Rectangle();

        //Inicializa sons
        somVoando = Gdx.audio.newSound( Gdx.files.internal("som_blaster.wav") );
        somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav") );
        somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav") );

        //Configura preferências dos objetos
        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima",0);

        //Configuração da câmera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose () {

    }
}