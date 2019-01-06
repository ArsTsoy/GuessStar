package com.example.arslan.guessstar2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    private Button button1, button2, button3, button4;
    private ImageView imageViewStar;


    private ArrayList<String> urls;
    private ArrayList<String> names;
    private ArrayList<Button> buttons;

    private int numberOfQuestion;
    private int numberOfRightAnswer;
    private int indexOfCurrentQuestion;


    private ArrayList<Bitmap> allImages;
    private String url = "http://www.posh24.se/kandisar";

    public static final String TAG = "MyTag";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageViewStar = findViewById(R.id.starPicture);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        indexOfCurrentQuestion = 0;

        urls = new ArrayList<>();
        names = new ArrayList<>();
        buttons = new ArrayList<>();
        buttons.add(button1);
        buttons.add(button2);
        buttons.add(button3);
        buttons.add(button4);


        getContent();
        Log.i("Stage", "getContentFinished");

        playGame();

        Log.i("Stage", "start");

    }

    private void playGame(){
        generateQuestion();

        ImageDownloader imageDownloader = new ImageDownloader();
        Log.i("Stage", "question is generated");
        Bitmap bitmap = null;
        try {
            bitmap = imageDownloader.execute(urls.get(0)).get();
            Log.i("Stage", "Bitmap is downloaded");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(imageViewStar != null){
            imageViewStar.setImageBitmap(bitmap);
        }else{
            imageViewStar.setImageResource(R.drawable.ic_cloud_off_black_24dp);
        }


        for (int i = 0; i < buttons.size(); i++) {
            if(i != numberOfRightAnswer){
                int wrongAnswer = generateWrongAnswer();
                buttons.get(i).setText(names.get(wrongAnswer));
            }else{
                buttons.get(numberOfRightAnswer).setText(names.get(0));
            }
        }


    }

    private int generateWrongAnswer(){
        return (int)(Math.random()*(names.size()-1)+1);
    }

    private void generateQuestion(){
        numberOfQuestion = (int)(Math.random()*(names.size()-indexOfCurrentQuestion)+indexOfCurrentQuestion);
        numberOfRightAnswer = (int)(Math.random()*buttons.size());

        String tempUrl = urls.get(numberOfQuestion);
        urls.remove(numberOfQuestion);
        urls.add(0,tempUrl);

        String temp = names.get(numberOfQuestion);
        names.remove(numberOfQuestion);
        names.add(0, temp);


        indexOfCurrentQuestion++;


    }


    private void getContent() {
        CodeDownloader codeDownloader = new CodeDownloader();
        try {
            String content = codeDownloader.execute(url).get();
            String start = "<p class=\"link\">Topp 100 kändisar</p>";
            String finish = "<div class=\"col-xs-12 col-sm-6 col-md-4\">";

            Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
            Matcher matcher = pattern.matcher(content);
            String splitContent = "";

            if (matcher.find()) {
                splitContent = matcher.group(1);
            }
//            splitContent = splitContent.replaceAll("\t", " ");


//            Log.i(TAG, splitContent);

            Pattern patternImg = Pattern.compile("<img src=\"(.*?)\"");
            Pattern patternName = Pattern.compile("alt=\"(.*?)\"/>");
            Matcher matcherImg = patternImg.matcher(splitContent);
            Matcher matcherName = patternName.matcher(splitContent);


            while (matcherImg.find()) {
                String downloadAddress = matcherImg.group(1);
                urls.add(downloadAddress);
                Log.i(TAG, downloadAddress);
            }
            while(matcherName.find()){
                String name = matcherName.group(1);
                Log.i(TAG, name);
                names.add(name);
            }


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void click(View view) {
        playGame();
    }


    private static class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {

            URL url = null;
            HttpURLConnection urlConnection = null;


            try {

                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                Log.i("Stage", "connection is opened");
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                Log.i("Stage", "bitmap generated");
//                inputStream.close();
                return bitmap;

            } catch (MalformedURLException e) {
                Log.e(TAG, "Ошибка в подключении(URL) во время скачивания");
            } catch (IOException e) {
                Log.e(TAG, "Ошибка в подключении(CONNECTION) во время скачивания");
                e.printStackTrace();
            }finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }


            return null;
        }
    }


    private static class CodeDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuilder pageCode = new StringBuilder();
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    pageCode.append(line);
                }
//                bufferedReader.close();
//                inputStreamReader.close();
//                inputStream.close();
                Log.i(TAG, pageCode.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "Ошибка в подключении во время чтения кода");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

            }


            return pageCode.toString();
        }
    }
}
