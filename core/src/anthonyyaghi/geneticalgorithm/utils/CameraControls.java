package anthonyyaghi.geneticalgorithm.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraControls {
    private static final int LEFT_KEY = Input.Keys.LEFT;
    private static final int RIGHT_KEY = Input.Keys.RIGHT;
    private static final int UP_KEY = Input.Keys.UP;
    private static final int DOWN_KEY = Input.Keys.DOWN;
    private static final int ZOOM_OUT = Input.Keys.SHIFT_RIGHT;
    private static final int ZOOM_IN = Input.Keys.CONTROL_RIGHT;
    private static final int RESET = Input.Keys.ENTER;


    private static final float MOVE_SPEED = 800.0f;
    private static final float ZOOM_SPEED = 2.0f;
    private static final float MAX_ZOOM_IN = 0.2f;
    private static final float MAX_ZOOM_OU = 30.0f;

    private Vector2 position = new Vector2();
    private Vector2 startPosition = new Vector2();
    private float zoom = 1.0f;

    public CameraControls() {
    }

    public void setStartPosition(float x, float y) {
        startPosition.set(x, y);
        position.set(x, y);
    }

    private void setZoom(float v) {
        zoom = MathUtils.clamp(v, MAX_ZOOM_IN, MAX_ZOOM_OU);
    }

    public void applyToCamera(OrthographicCamera camera) {
        camera.position.set(position, 0);
        camera.zoom = zoom;
        camera.update();
    }

    public void handleInput(float delta) {
        float moveSpeed = MOVE_SPEED * delta;
        float zoomSpeed = ZOOM_SPEED * delta;

        if (Gdx.input.isKeyPressed(LEFT_KEY)) {
            moveLeft(moveSpeed);
        } else if (Gdx.input.isKeyPressed(RIGHT_KEY)) {
            moveRight(moveSpeed);
        }

        if (Gdx.input.isKeyPressed(UP_KEY)) {
            moveUp(moveSpeed);
        } else if (Gdx.input.isKeyPressed(DOWN_KEY)) {
            moveDown(moveSpeed);
        }

        if (Gdx.input.isKeyPressed(ZOOM_IN)) {
            zoomIn(zoomSpeed);
        } else if (Gdx.input.isKeyPressed(ZOOM_OUT)) {
            zoomOut(zoomSpeed);
        }

        if (Gdx.input.isKeyPressed(RESET)) {
            resetView();
        }
    }

    private void resetView() {
        position.set(startPosition);
        setZoom(1.0f);
    }

    private void zoomOut(float zoomSpeed) {
        setZoom(zoom - zoomSpeed);
    }

    private void zoomIn(float zoomSpeed) {
        setZoom(zoom + zoomSpeed);
    }

    private void setPosition(float x, float y) {
        position.set(x, y);
    }

    private void moveCamera(float xSpeed, float ySpeed) {
        setPosition(position.x + xSpeed, position.y + ySpeed);
    }

    private void moveLeft(float moveSpeed) {
        moveCamera(-moveSpeed, 0);
    }

    private void moveRight(float moveSpeed) {
        moveCamera(moveSpeed, 0);
    }

    private void moveUp(float moveSpeed) {
        moveCamera(0, moveSpeed);
    }

    private void moveDown(float moveSpeed) {
        moveCamera(0, -moveSpeed);
    }

}
