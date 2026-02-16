# Non-Stationary Load Balancing with Softmax Action Selection

Bu proje, dağıtık sistemlerde değişen sunucu performanslarına (non-stationary distribution) uyum sağlayabilen, olasılıksal bir yük dengeleyici (Load Balancer) tasarımıdır. Klasik Round-Robin ve Random yaklaşımları, geçmiş veriyi kullanarak öğrenen Softmax Action Selection algoritması ile kıyaslanmaktadır.

## 🚀 Problem Tanımı
Gerçek dünya sistemlerinde sunucu yanıt süreleri (latency) sabit değildir; ağ yoğunluğu, CPU yükü veya Java GC gibi nedenlerle zamanla değişir ve gürültülüdür. Bu projede:
- **K adet** farklı sunucu bulunmaktadır.
- Sunucu performansları **Random Walk** modeliyle zamanla değişmektedir.
- Hedef, toplam gecikmeyi minimize eden **optimal sunucu seçim stratejisini** geliştirmektir.

## 🧠 Algoritma: Softmax Action Selection
Softmax, her sunucuya bir olasılık değeri atayarak "Keşif" (Exploration) ve "Sömürü" (Exploitation) dengesini kurar.

### Matematiksel Model
Sunucu $i$ için seçim olasılığı:
$$P_t(a) = \frac{e^{Q_t(a) / \tau}}{\sum_{i=1}^{K} e^{Q_t(i) / \tau}}$$

* **$Q_t(a)$**: Sunucunun geçmişteki ortalama performansı.
* **$\tau$ (Temperature)**: Yüksek değerlerde sistem daha fazla keşif yapar, düşük değerlerde en iyi sunucuya odaklanır.

### Nümerik Stabilite
Üstel fonksiyon ($e^x$) çok büyük değerler üretebileceğinden (Floating point overflow), olasılıklar hesaplanmadan önce tüm $Q$ değerlerinden o anki $\max(Q)$ değeri çıkarılarak hesaplama yapılmıştır.

## 🛠 Kullanılan Teknolojiler
- **Dil:** Java 11+
- **Paradigma:** Nesne Yönelimli Programlama ve Agentic Kodlama
- **IDE:** VS Code / IntelliJ IDEA

## 📊 Analiz ve Sonuçlar
Simülasyon sonuçları, Softmax algoritmasının zamanla yavaşlayan sunucuları "terk ettiğini" ve hızlanan sunuculara trafiği yönlendirdiğini kanıtlamaktadır.
- **Round-Robin:** Performans değişimlerini kördür, her sunucuya eşit yük verir.
- **Softmax:** En düşük ortalama gecikmeyi (average latency) sağlar.

## 🏁 Çalıştırma
```bash
javac LoadBalancerSim.java
java LoadBalancerSim
