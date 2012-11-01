package com.plumtree.catcher;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.TiledSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PhysicsConnector;
import com.badlogic.gdx.physics.box2d.PhysicsFactory;
import com.badlogic.gdx.physics.box2d.PhysicsWorld;
import com.badlogic.gdx.physics.box2d.Vector2Pool;

import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

public class MainActivity extends BaseGameActivity implements IAccelerometerListener,IOnSceneTouchListener,IOnAreaTouchListener{
	
	 private static final int CAMERA_WIDTH = 360;
     private static final int CAMERA_HEIGHT = 240;

     // ===========================================================
     // Fields
     // ===========================================================
     


     private BitmapTextureAtlas mBitmapTextureAtlas;

     private TiledTextureRegion mBoxFaceTextureRegion;
     private TiledTextureRegion mCircleFaceTextureRegion;

     private int mFaceCount = 0;

     private  PhysicsWorld mPhysicsWorld;

     private float mGravityX;
     private float mGravityY;
     private Scene mScene;


	@Override
	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Engine onLoadEngine() {
		// TODO Auto-generated method stub
		EngineOptions engineOptions=null;
		try{
			final Display display = getWindowManager().getDefaultDisplay();
			 Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();
	         final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
	         engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	         engineOptions.getTouchOptions().setRunOnUpdateThread(true);
         }
		catch(Exception ex){
			Log.v("oLE",ex.getMessage() + "");
		}finally{
			return new Engine(engineOptions);
		}
 }

	
	@Override
	public void onLoadResources() {
		// TODO Auto-generated method stub
		try{
		 this.mBitmapTextureAtlas = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "img1.png", 0, 0, 2, 1); // 64x32
         this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "img1.png", 0, 32, 2, 1); // 64x32
         this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
		} catch(Exception ex){
			Log.v("oLR", ex.getMessage() + "");
		}

	}

	@SuppressWarnings("finally")
	@Override
	public Scene onLoadScene() {
		// TODO Auto-generated method stub
		 
		 try{
			 this.mEngine.registerUpdateHandler(new FPSLogger());
	         this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
	         this.mScene = new Scene();
	         this.mScene.setBackground(new ColorBackground(0, 0, 0));
	         this.mScene.setOnSceneTouchListener(this);
	         
	         Entity move = null;
			final Entity playerEntity = move;
	         final float jumpDuration = 1;
	         final float startX = playerEntity.getX();
	         final float jumpHeight = 60;
	         ((TiledSprite) move).getTextureRegion().setFlippedVertical(true);
	         final MoveModifier jumpForwardUp = new MoveModifier(jumpDuration/2, startX,
	        		 startX-jumpHeight, playerEntity.getY(), playerEntity.getY()+130);
	         final MoveModifier jumpForwardDown = new MoveModifier(jumpDuration/2,
	        		 startX-jumpHeight, startX, playerEntity.getY()+130, playerEntity.getY()+170);
	         final SequenceEntityModifier modifier = new SequenceEntityModifier(jumpForwardUp, jumpForwardDown);
	         playerEntity.registerEntityModifier(modifier);
	
	 /*        final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
	         final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
	         final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
	         final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
	
	         final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
	         PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
	         PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
	         PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
	         PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
	
	         this.mScene.attachChild(ground);
	         this.mScene.attachChild(roof);
	         this.mScene.attachChild(left);
	         this.mScene.attachChild(right);
	
	         this.mScene.registerUpdateHandler(this.mPhysicsWorld);
	
	 */        this.mScene.setOnAreaTouchListener(this);
         }catch (IllegalStateException e){
        	 Log.v("onLoadScene", e.getMessage().toString());
         }finally {
        	 return this.mScene;
         }

	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		try{
		if(pSceneTouchEvent.isActionDown()) {
            final AnimatedSprite face = (AnimatedSprite) pTouchArea;
            this.jumpFace(face);
            return true;
    }

		}
		catch(Exception ex){
			Log.v("oAT", ex.getMessage() + "");
		}return false;
	}

	

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		try{
		if(this.mPhysicsWorld != null) {
            if(pSceneTouchEvent.isActionDown()) {
                    this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                    return true;
            }
    }
		}catch (Exception ex){
			Log.v("oSTE", ex.getMessage() + "");
		}

		return false;
	}



	@Override
	public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) {
		// TODO Auto-generated method stub
		try {
			this.mGravityX = pAccelerometerData.getX();
	        this.mGravityY = pAccelerometerData.getY();

	        final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
	        this.mPhysicsWorld.setGravity(gravity);
	        Vector2Pool.recycle(gravity);
		} catch (Exception ex) {
			Log.v("oAC", ex.getMessage()+"");
		}

	}
	
	 @Override
     public void onResumeGame() {
             super.onResumeGame();
             this.enableAccelerometerSensor(this);
     }

     @Override
     public void onPauseGame() {
             super.onPauseGame();

             this.disableAccelerometerSensor();
     }

	
	private void addFace(final float pX, final float pY) {
		// TODO Auto-generated method stub
		 try {
			 this.mFaceCount++;

	         final AnimatedSprite face;
	         final Body body;

	         final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	         if(this.mFaceCount % 2 == 1){
	                 face = new AnimatedSprite(pX, pY, this.mBoxFaceTextureRegion);
	                 body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
	         } else {
	                 face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion);
	                 body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
	         }

	         this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));

	         face.animate(new long[]{200,200}, 0, 1, true);
	         face.setUserData(body);
	         this.mScene.registerTouchArea(face);
	         this.mScene.attachChild(face);
		 } catch (Exception ex) {
			 Log.v("aF", ex.getMessage() + "");
		 }

		
	}
	
	private void jumpFace(AnimatedSprite face) {
		// TODO Auto-generated method stub
		 try {
			 final Body faceBody = (Body)face.getUserData();

	         final Vector2 velocity = Vector2Pool.obtain(this.mGravityX * -50, this.mGravityY * -50);
	         faceBody.setLinearVelocity(velocity);
	         Vector2Pool.recycle(velocity);
		 } catch (Exception ex) {
			 Log.v("jF", ex.getMessage()+ "");
		 }

	}


}
