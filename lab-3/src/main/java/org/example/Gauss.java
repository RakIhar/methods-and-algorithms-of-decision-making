package org.example;

record InputValues(double mu1, double mu2, double sigma1, double sigma2, double priorProb1) {}

record Probabilities(double probabilityOfMissingError,
                     double probabilityOfFalseAlarm,
                     double probabilityOfTotalClassificationError) { }

public class Gauss {
    public static Probabilities getProbabilities(InputValues vals) {
        if (vals.priorProb1() < 0 || vals.priorProb1() > 1)
            return null;

        double mu1 = vals.mu1();
        double mu2 = vals.mu2();
        double sigma1 = Math.sqrt(vals.sigma1());
        double sigma2 = Math.sqrt(vals.sigma2());
        double p1 = vals.priorProb1();
        double p2 = 1.0 - p1;

        // X*
        double xStar = findThreshold(mu1, mu2, sigma1, sigma2, p1, p2);

        // Вероятность ложной тревоги
        double pFalseAlarm = p2 * normalCDF(xStar, mu2, sigma2);

        // Вероятность пропуска
        double pMissing = p1 * (1.0 - normalCDF(xStar, mu1, sigma1));

        double pTotal = pFalseAlarm + pMissing;

        return new Probabilities(pMissing, pFalseAlarm, pTotal);
    }

    /**
     * X* через решение уравнения p(x|C1)P(C1) = p(x|C2)P(C2)
     */
    //a = σ₂² − σ₁²
    //b = 2(μ₂σ₁² − μ₁σ₂²)
    //c = μ₁²σ₂² − μ₂²σ₁² − 2σ₁²σ₂²·ln((P(C₁)σ₂)/(P(C₂)σ₁))
    private static double findThreshold(double m1, double m2, double s1, double s2, double p1, double p2) {
        if (Math.abs(s1 - s2) < 1e-9) {
            return (m1 + m2) / 2.0 + (s1 * s1 / (m2 - m1)) * Math.log(p2 / p1);
        }

        double a = s2 * s2 - s1 * s1;
        double b = 2 * (m2 * s1 * s1 - m1 * s2 * s2);
        double c = m1 * m1 * s2 * s2 - m2 * m2 * s1 * s1 - 2 * s1 * s1 * s2 * s2 * Math.log((p1 * s2) / (p2 * s1));

        double D = b * b - 4 * a * c;
        if (D < 0)
            return (m1 + m2) / 2.0;

        double x1 = (-b + Math.sqrt(D)) / (2 * a);
        double x2 = (-b - Math.sqrt(D)) / (2 * a);

        double minMu = Math.min(m1, m2);
        double maxMu = Math.max(m1, m2);

        if (x1 >= minMu && x1 <= maxMu) return x1;
        return x2;
    }

    /**
     * Φ(x)=P(X≤x)
     */
    private static double normalCDF(double x, double mu, double sigma) {
        return 0.5 * (1 + erf((x - mu) / (sigma * Math.sqrt(2))));
    }

    private static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
        double ans = 1 - t * Math.exp(-z * z - 1.26551223 +
                t * (1.00002368 +
                        t * (0.37409196 +
                                t * (0.09678418 +
                                        t * (-0.18628806 +
                                                t * (0.27886807 +
                                                        t * (-1.13520398 +
                                                                t * (1.48851587 +
                                                                        t * (-0.82215223 +
                                                                                t * (0.17087277))))))))));
        return z >= 0 ? ans : -ans;
    }
}