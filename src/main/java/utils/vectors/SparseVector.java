package utils.vectors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SparseVector {
    public SparseVector() {
        this.elements = new HashMap<>();
    }

    private final Map<Integer, Double> elements;

    public void set(int index, double value) {
        checkBounds(index);
        if (value == 0.0) {
            elements.remove(index);
        } else {
            elements.put(index, value);
        }
    }

    public double get(int index) {
        checkBounds(index);
        return elements.getOrDefault(index, 0.0);
    }

    public double dot(SparseVector other) {
        double result = 0.0;
        if (this.elements.size() < other.elements.size()) {
            for (Map.Entry<Integer, Double> entry : this.elements.entrySet()) {
                result += entry.getValue() * other.elements.getOrDefault(entry.getKey(), 0.0);
            }
        } else {
            for (Map.Entry<Integer, Double> entry : other.elements.entrySet()) {
                result += entry.getValue() * this.elements.getOrDefault(entry.getKey(), 0.0);
            }
        }
        return result;
    }

    public double len() {
        double sumSquares = 0.0;
        for (double value : elements.values()) {
            sumSquares += value * value;
        }
        return Math.sqrt(sumSquares);
    }

    public void add(SparseVector other) {
        for (Map.Entry<Integer, Double> entry : other.elements.entrySet()) {
            int index = entry.getKey();
            double sum = this.get(index) + entry.getValue();
            this.set(index, sum);
        }
    }

    public void multiply(SparseVector other) {
        for (Map.Entry<Integer, Double> entry : this.elements.entrySet()) {
            int index = entry.getKey();
            if (other.elements.containsKey(index)) {
                double product = entry.getValue() * other.get(index);
                this.set(index, product);
            }
        }
    }

    public void multiply(double scalar) {
        for (Map.Entry<Integer, Double> entry : this.elements.entrySet()) {
            this.set(entry.getKey(), entry.getValue() * scalar);
        }
    }

    public void toUnitVector(){
        this.multiply(1 / this.len());
    }

    public double angleTo(SparseVector other) {
        double dot = this.dot(other);
        double lenProduct = this.len() * other.len();
        if (lenProduct == 0.0) {
            return Double.NaN;
        }
        double cosTheta = dot / lenProduct;
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));
        return Math.acos(cosTheta);
    }

    public Set<Integer> getNonZeroEntries(){
        return elements.keySet();
    }

    private void checkBounds(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Vector index must be greater than or equal to zero");
        }
    }

    @Override
    public String toString() {
        return "SparseVector" + elements;
    }
}
