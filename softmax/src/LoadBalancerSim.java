import java.util.*;

/**
 * Dağıtık Sistemler için Softmax Load Balancer Simülasyonu
 * Bu uygulama Softmax, Round-Robin ve Random algoritmalarını kıyaslar.
 */
public class LoadBalancerSim {

    // --- Konfigürasyon ---
    private static final int K = 5;             // Sunucu sayısı
    private static final int STEPS = 10000;     // Simülasyon adım sayısı
    private static final double ALPHA = 0.1;    // Öğrenme hızı (Non-stationary için sabit adım)
    private static final double TAU = 2.0;      // Sıcaklık parametresi (Exploration/Exploitation)

    public static void main(String[] args) {
        System.out.println("=== Load Balancer Simülasyonu Başlıyor ===");

        // Sunucuları oluştur
        Server[] servers = new Server[K];
        for (int i = 0; i < K; i++) servers[i] = new Server(i);

        // Balancer'ları tanımla
        Balancer softmax = new SoftmaxBalancer(K, TAU, ALPHA);
        Balancer roundRobin = new RoundRobinBalancer(K);
        Balancer random = new RandomBalancer(K);

        // İstatistik tutucular
        double totalSoftmaxLat = 0, totalRRLat = 0, totalRandLat = 0;

        // Simülasyon Döngüsü
        for (int t = 0; t < STEPS; t++) {
            // 1. Sunucu durumlarını güncelle (Non-stationary: Latency değerleri drift eder)
            for (Server s : servers) s.drift();

            // 2. Softmax Seçimi ve Güncelleme
            int sIdx = softmax.select();
            double lat = servers[sIdx].getLatency();
            softmax.update(sIdx, lat);
            totalSoftmaxLat += lat;

            // 3. Round Robin
            totalRRLat += servers[roundRobin.select()].getLatency();

            // 4. Random
            totalRandLat += servers[random.select()].getLatency();
        }

        // Sonuçları Yazdır
        printResults("Softmax", totalSoftmaxLat);
        printResults("Round-Robin", totalRRLat);
        printResults("Random", totalRandLat);
    }

    private static void printResults(String name, double totalLat) {
        System.out.printf("%-15s | Avg Latency: %.2f ms | Total: %.0f ms%n",
                name, (totalLat / STEPS), totalLat);
    }

    // --- Core Bileşenler ---

    interface Balancer {
        int select();
        void update(int id, double latency);
    }

    /**
     * Softmax Action Selection Algoritması
     */
    static class SoftmaxBalancer implements Balancer {
        private final double[] qValues; // Tahmin edilen ödüller (negatif latency)
        private final double tau;
        private final double alpha;
        private final Random rand = new Random();

        public SoftmaxBalancer(int k, double tau, double alpha) {
            this.qValues = new double[k];
            this.tau = tau;
            this.alpha = alpha;
        }

        @Override
        public int select() {
            double[] probs = calculateProbabilities();
            double r = rand.nextDouble();
            double cumulative = 0;
            for (int i = 0; i < probs.length; i++) {
                cumulative += probs[i];
                if (r <= cumulative) return i;
            }
            return probs.length - 1;
        }

        /**
         * Nümerik Stabilite Çözümü: exp(x - max)
         */
        private double[] calculateProbabilities() {
            double maxQ = Double.NEGATIVE_INFINITY;
            for (double q : qValues) if (q > maxQ) maxQ = q;

            double sum = 0;
            double[] expValues = new double[qValues.length];
            for (int i = 0; i < qValues.length; i++) {
                // Stabilite için maxQ çıkarılır, tau'ya bölünür
                expValues[i] = Math.exp((qValues[i] - maxQ) / tau);
                sum += expValues[i];
            }

            for (int i = 0; i < expValues.length; i++) expValues[i] /= sum;
            return expValues;
        }

        @Override
        public void update(int id, double latency) {
            // Reward = -latency (Düşük latency, yüksek ödüldür)
            double reward = -latency;
            // Constant step-size update for non-stationary environments
            qValues[id] += alpha * (reward - qValues[id]);
        }
    }

    static class RoundRobinBalancer implements Balancer {
        private int current = 0;
        private final int k;
        public RoundRobinBalancer(int k) { this.k = k; }
        public int select() { int res = current; current = (current + 1) % k; return res; }
        public void update(int id, double l) {}
    }

    static class RandomBalancer implements Balancer {
        private final Random r = new Random();
        private final int k;
        public RandomBalancer(int k) { this.k = k; }
        public int select() { return r.nextInt(k); }
        public void update(int id, double l) {}
    }

    /**
     * Non-stationary Sunucu Modeli
     */
    static class Server {
        private final int id;
        private double baseLatency;
        private final Random rand = new Random();

        public Server(int id) {
            this.id = id;
            this.baseLatency = 20 + rand.nextDouble() * 30; // 20-50ms arası başlangıç
        }

        public void drift() {
            // Zamanla performans değişimi (Random Walk)
            baseLatency += rand.nextGaussian() * 0.5;
            if (baseLatency < 5) baseLatency = 5; // Alt sınır
        }

        public double getLatency() {
            // Anlık gürültü (Noise)
            return baseLatency + rand.nextGaussian() * 2;
        }
    }
}