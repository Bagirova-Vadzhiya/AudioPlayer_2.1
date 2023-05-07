package com.example.audioplayer_21;




import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements Runnable {

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private boolean wasPlaying = false;
    private FloatingActionButton fabPlayPause;
    private FloatingActionButton fabBack;
    private FloatingActionButton fabRepeat;
    private FloatingActionButton fabForward;
    private FloatingActionButton fabNext;
    private TextView timeDuration;
    private TextView metaDataAudio;
    private String metaData;
    private boolean isRepeat = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присваивание полям id ресурсов
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabBack = findViewById(R.id.fabBack);
        fabRepeat = findViewById(R.id.fabRepeat);
        fabForward = findViewById(R.id.fabForward);
        fabNext = findViewById(R.id.fabNext);
        timeDuration = findViewById(R.id.timeDuration);
        seekBar = findViewById(R.id.seekBar);
        metaDataAudio = findViewById(R.id.metaDataAudio);

        // создание слушателя изменения SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // метод при перетаскивании ползунка по шкале,
            // где progress позволяет получить нове значение ползунка (позже progress назрачается длина трека в миллисекундах)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeDuration.setVisibility(View.VISIBLE); // установление видимости timeDuration
                //timeDuration.setVisibility(View.INVISIBLE); // установление не видимости timeDuration

                // Math.ceil() - округление до целого в большую сторону
                int timeTrack = (int) Math.ceil(progress / 1000f); // перевод времени из миллисекунд в секунды
                int minute = timeTrack / 60;
                // вывод на экран времени отсчёта трека
                timeDuration.setText(String.format("%02d", minute) + ":" + String.format("%02d", (timeTrack % 60)));


                // передвижение времени отсчёта трека
                double percentTrack = progress / (double) seekBar.getMax(); // получение процента проигранного трека (проигранное время делится на длину трека)
                // seekBar.getX() - начало seekBar по оси Х
                // seekBar.getWidth() - ширина контейнера seekBar
                // 0.92 - поправочный коэффициент (так как seekBar занимает не всю ширину своего контейнера)
                timeDuration.setX(seekBar.getX() + Math.round(seekBar.getWidth() * percentTrack * 0.92));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится
                    clearMediaPlayer(); // остановка и очистка MediaPlayer
                    MainActivity.this.seekBar.setProgress(0); // установление seekBar значения 0
                    // назначение кнопке картинки play
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                }
            }

            // метод при начале перетаскивания ползунка по шкале
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                timeDuration.setVisibility(View.INVISIBLE); // установление видимости timeDuration
            }

            // метод при завершении перетаскивания ползунка по шкале
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });


        // обработка нажатия кнопки
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
        fabRepeat.setOnClickListener(listener);
        fabNext.setOnClickListener(listener);

    }

    // создание слушателя нажатия кнопки
        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // обработка нажатия нескольких кнопок
                switch (view.getId()) {
                    case R.id.fabPlayPause:
                        playSong(); // воспроизведение музыки
                        break;
                    case R.id.fabBack:
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                        break;
                    case  R.id.fabForward:
                            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                        break;
                    case R.id.fabRepeat:
                        if (!isRepeat && mediaPlayer != null) {
                            mediaPlayer.setLooping(true); // включение повтора аудио
                            // назначение кнопке картинки
                            fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.repeat));
                            isRepeat = true;
                        } else if (isRepeat && mediaPlayer != null) {
                            mediaPlayer.setLooping(false); // выключение повтора аудио
                            // назначение кнопке картинки
                            fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.repeat_off));
                            isRepeat = false;
                        }
                        break;
                    case R.id.fabNext:

                        break;
                }

            }
        };


    public void playSong() {
        try { // обработка исключения на случай отстутствия файла
            if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                clearMediaPlayer(); // остановка и очиска MediaPlayer
                wasPlaying = true; // инициализация значения запуска аудио-файла
                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) { // если mediaPlayer пустой
                    mediaPlayer = new MediaPlayer(); // то выделяется для него память
                }
                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));

                // альтернативный способ считывания файла с помощью файлового дескриптора
                AssetFileDescriptor descriptor = getAssets().openFd("Дворжак - Симфония №9 (Из Нового Света) 4 часть.mp3");
                // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                // запись метаданных в окно mataDataAudio
                MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever(); // создание объекта специального класса для считывания метаданных
                mediaMetadata.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength()); // считывания метаданных по имеющемуся дискриптору

                metaData =  mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); // добавления артиста
                metaData += "\n" + mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); // добавление названия трека

                mediaMetadata.release(); // закрытие объекта загрузки метаданных

                metaDataAudio.setText(metaData); // вывод метаданных в окно metaDataAudio

                descriptor.close(); // закрытие дескриптора

                mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                mediaPlayer.setVolume(0.7f, 0.7f); // задание уровня громкости левого и правого динамиков
                mediaPlayer.setLooping(false); // назначение отстутствия повторов
                seekBar.setMax(mediaPlayer.getDuration()); // ограниечение seekBar длинной трека
                mediaPlayer.seekTo(seekBar.getProgress());

                mediaPlayer.start(); // старт mediaPlayer
                new Thread(this).start(); // запуск дополнительного потока
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }
    }

    // при уничтожении активности вызов метода остановки и очистки MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    // метод остановки и очистки MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление ресурсов
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при установки не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека
            } catch (InterruptedException e) { // вызывание в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }
            seekBar.setProgress(currentPosition); // обновление seekBar текущей позиции трека
        }
    }
}