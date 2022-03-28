# Android-Quiz-App-Firebase
Android multiplayer quiz app using Firebase real-time database.
<p>JSON file: <a href="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/quiz-app.json">Click here</a></p>
<p><img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/quizApp.gif" width="50%" height="50%"></p>

<h1>TR</h1>
<p>Çok oyunculu android soru-cevap uygulaması.</p>

<h5>2 oyuncunun eşleşmesi:</h5>
<p>Kullanıcı uygulamaya giriş yaptıktan sonra "play" butonuna basar. Eşleşme bekleyen kullanıcı mevcutsa (müsait oda varsa) 2.oyuncu olarak odaya giriş yapılır. Eğer müsait oda yoksa veya mevcut odaların hepsi doluysa 1.oyuncu olarak yeni oda oluşturulur ve 2.oyuncunun odaya giriş yapması beklenir. Eşleşme gerçekleştikten sonra oyun başlar.</p>

<h5>Oyunun mantığı:</h5>
<p>Soru havuzundan rastgele 10 soru çekilir. Bu soruların beş tanesi 1.oyuncuya, diğer beş tanesi de 2.oyuncuya sorulur. Oyunun sonunda doğru cevap sayısı fazla olan oyuncu, oyunu kazanır. Oyuncuların doğru cevap sayıları eşitse oyun beraberedir.</p>

<h5>Soru cevaplama sırası:</h5>
<p>Soru cevaplama sırası 1.oyuncudan başlar ve 2.oyuncudan devam eder. Soruyu cevaplamak için oyuncuya tanınan süre 30 saniyedir. Eğer oyuncu, 30 saniye içerisinde soruyu yanıtlamazsa o sorudan tik alamaz ve rakip oyuncunun sorusuyla oyun devam eder.</p>

<h5>Oyun sonu:</h5>
<p>Oyuncu galibiyet durumunda 20 puan, beraberlik durumunda 10 puan kazanır. Mağlubiyet durumunda ise oyuncu puan kazanamaz.</p>

<h5>Seviye:</h5>
<p>Her 100 puan, oyuncuya 1 seviye atlatır.</p>

<h1>Screenshots</h1>
<p><img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-201804.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-201810.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-201819.png" width="20%" height="20%"></p>
<p><img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-201849.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-201950.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-202026.png" width="20%" height="20%"></p>
<p><img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-202057.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-202113.png" width="20%" height="20%"> <img src="https://github.com/samet-ozkan/Android-Quiz-App-Firebase/blob/main/Screenshots/Screenshot_20220328-202340.png" width="20%" height="20%"></p>
