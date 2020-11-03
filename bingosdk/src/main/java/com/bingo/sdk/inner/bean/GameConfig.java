package com.bingo.sdk.inner.bean;

public class GameConfig {
    private Game game;

    public Game getGame() {
        return game;
    }

    public GameConfig setGame(Game game) {
        this.game = game;
        return this;
    }

    @Override
    public String toString() {
        return "GameConfig{" +
                "game=" + game +
                '}';
    }
}
