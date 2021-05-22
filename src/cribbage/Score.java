package cribbage;

public interface Score {
    default int getScore() {
        return 0;
    }
}
