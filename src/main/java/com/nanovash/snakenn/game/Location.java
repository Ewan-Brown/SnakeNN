package com.nanovash.snakenn.game;

import com.nanovash.snakenn.game.util.LossException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of={"x", "y"})
public class Location {
    private @Getter @Setter int x;
    private @Getter @Setter int y;

    public Location(int x, int y, boolean walls) throws LossException {
        if(walls) {
            this.x = x;
            this.y = y;
            if (x < 0 || x >= GameModel.SIDE || y < 0 || y >= GameModel.SIDE)
                throw new LossException();
        }
        else {
            this.x = getCorrectNumber(x);
            this.y = getCorrectNumber(y);
        }
    }

    public int getCorrectNumber(int i) {
        return i >= GameModel.SIDE ? i - GameModel.SIDE : i < 0 ? i + GameModel.SIDE : i;
    }
}