package com.medikok.frontend.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.TimePicker;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.medikok.frontend.R;
import com.medikok.frontend.activity.DrugDetailActivity;
import com.medikok.frontend.model.DrugInfo;
import com.medikok.frontend.util.AddSchedule;
import com.medikok.frontend.util.ServerConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlarmFragment extends Fragment {
    List<String> drugNameList = new ArrayList<>();

    List<DrugInfo> drugInfoList = new ArrayList<>(); // 약에 대한 정보들과 정보 요청 메소드들이 담긴 리스트 선언

    String selectedItem;

    private LinearLayout alarmContainer;

    public AlarmFragment() {
        // Required empty public constructor
    }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static AlarmFragment newInstance(String param1, String param2) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ServerConnector serverConnector;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        // 알람 표시 동적 구현
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        alarmContainer = view.findViewById(R.id.alarmContainer);
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        FloatingActionButton btn_test = view.findViewById(R.id.btn_test);

        // 플로팅 버튼 클릭 이벤트 처리
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDayPickerDialog();
            }
        });

        // Test 버튼 클릭 이벤트 처리
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다른 화면으로 전환하기 위한 Intent 생성
                Intent intent = new Intent(getActivity(), AddSchedule.class);
                startActivity(intent); // Intent를 사용하여 새로운 화면으로 전환
            }
        });

        ServerConnector.connectToServer(new ServerConnector.ServerResponseListener() {
            @Override
            public void onSuccess(List<DrugInfo> responseData) {

                // drugInfoList에 서버로부터 받은 데이터 저장
                drugInfoList = responseData;

                LinearLayout dynamicLayout = view.findViewById(R.id.layout1);
                for (DrugInfo drugInfo : responseData) {

                    Log.d("AlarmFragment", "Item Name: " + drugInfo.getItemName());
                    Log.d("AlarmFragment", "Effects: " + drugInfo.getEfcyQesitm());
                    Log.d("AlarmFragment", "Use Method: " + drugInfo.getUseMethodQesitm());
                    Log.d("AlarmFragment", "Item Image: " + drugInfo.getItemImage());

                    String name = drugInfo.getItemName();
                    String effect = drugInfo.getEfcyQesitm();
                    String method = drugInfo.getUseMethodQesitm();
                    String imageUrl = drugInfo.getItemImage();

//                    CardView pillCard = makePillCard(getContext(), imageUrl, name, method, effect);
//                    dynamicLayout.addView(pillCard);

                    // 가져온 데이터를 drugNameList에 저장
                    drugNameList.add(drugInfo.getItemName());
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle the error
            }
        });

        loadAlarmsFromPreferences();  // Load alarms from SharedPreferences when the fragment is created

        return view;
    }

    // 플로팅 버튼을 클릭 했을 경우
    private void showTimeDayPickerDialog() {
        Context context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_day_picker, null);

        // 약 선택 드롭 다운 어댑터 설정
        Spinner dropDownList = dialogView.findViewById(R.id.dropDownList);
        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                drugNameList
        );
        dropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDownList.setAdapter(dropDownAdapter);

        dropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무 항목도 선택되지 않았을 때의 동작 (필요 시 구현)
            }
        });

        builder.setView(dialogView);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        CheckBox[] dayCheckBoxes = new CheckBox[] {
                dialogView.findViewById(R.id.checkMonday),
                dialogView.findViewById(R.id.checkTuesday),
                dialogView.findViewById(R.id.checkWednesday),
                dialogView.findViewById(R.id.checkThursday),
                dialogView.findViewById(R.id.checkFriday),
                dialogView.findViewById(R.id.checkSaturday),
                dialogView.findViewById(R.id.checkSunday)
        };

        // 확인 버튼을 누를 경우
        builder.setPositiveButton("확인", (dialog, which) -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout newAlarmCard = (LinearLayout) inflater.inflate(R.layout.alarm, null);

            TextView alarmTime = newAlarmCard.findViewById(R.id.alarmPillDateTime);

            // 알람 카드에 선택한 약 이름 설정
            TextView alarmPillName = newAlarmCard.findViewById(R.id.alarmPillName);

            alarmTime.setText(String.format("%02d:%02d", hour, minute));
            alarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);

            alarmPillName.setText(selectedItem);

            TextView[] dayTextViews = new TextView[] {
                    newAlarmCard.findViewById(R.id.alarmPillDateMon),
                    newAlarmCard.findViewById(R.id.alarmPillDateTue),
                    newAlarmCard.findViewById(R.id.alarmPillDateWed),
                    newAlarmCard.findViewById(R.id.alarmPillDateThu),
                    newAlarmCard.findViewById(R.id.alarmPillDateFri),
                    newAlarmCard.findViewById(R.id.alarmPillDateSat),
                    newAlarmCard.findViewById(R.id.alarmPillDateSun)
            };

            TextView everyDayTextView = newAlarmCard.findViewById(R.id.alarmPillEveryday);

            boolean allChecked = true;
            for (CheckBox checkBox : dayCheckBoxes) {
                if (!checkBox.isChecked()) {
                    allChecked = false;
                    break;
                }
            }

            if (allChecked) {
                for (TextView dayTextView : dayTextViews) {
                    updateDayTextView(dayTextView, true);
                }
                everyDayTextView.setVisibility(View.VISIBLE);
            } else {
                for (int i = 0; i < dayCheckBoxes.length; i++) {
                    updateDayTextView(dayTextViews[i], dayCheckBoxes[i].isChecked());
                }
                everyDayTextView.setVisibility(View.INVISIBLE);
            }

            alarmContainer.addView(newAlarmCard);

            addAlarmCardEventListeners(newAlarmCard);
            saveAlarmToPreferences(newAlarmCard);  // Save the new alarm to SharedPreferences

            LinearLayout dynamicLayout = getView().findViewById(R.id.layout1);
            for (DrugInfo drugInfo : drugInfoList) {
                if (drugInfo.getItemName().equals(alarmPillName.getText().toString())) {
                    String name = drugInfo.getItemName();
                    String effect = drugInfo.getEfcyQesitm();
                    String method = drugInfo.getUseMethodQesitm();
                    String imageUrl = drugInfo.getItemImage();

                    CardView pillCard = makePillCard(getContext(), imageUrl, name, method, effect);
                    dynamicLayout.addView(pillCard);
                }
            }
        });

        builder.setNegativeButton("취소", null);
        builder.create().show();
    }

    private void showEditTimeDayPickerDialog(LinearLayout alarmCard) {
        Context context = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_day_picker, null);

        // 약 선택 드롭 다운 어댑터 설정
        Spinner dropDownList = dialogView.findViewById(R.id.dropDownList);
        ArrayAdapter<String> dropDownAdapter = new ArrayAdapter<>(
                requireActivity(),
                android.R.layout.simple_spinner_item,
                drugNameList
        );
        dropDownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropDownList.setAdapter(dropDownAdapter);

        dropDownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무 항목도 선택되지 않았을 때의 동작 (필요 시 구현)
            }
        });

        builder.setView(dialogView);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        CheckBox[] dayCheckBoxes = new CheckBox[] {
                dialogView.findViewById(R.id.checkMonday),
                dialogView.findViewById(R.id.checkTuesday),
                dialogView.findViewById(R.id.checkWednesday),
                dialogView.findViewById(R.id.checkThursday),
                dialogView.findViewById(R.id.checkFriday),
                dialogView.findViewById(R.id.checkSaturday),
                dialogView.findViewById(R.id.checkSunday)
        };

        TextView alarmTime = alarmCard.findViewById(R.id.alarmPillDateTime);

        // 알람 카드에 선택한 약 이름 설정
        TextView alarmPillName = alarmCard.findViewById(R.id.alarmPillName);

        String[] timeParts = alarmTime.getText().toString().split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1]);
        timePicker.setHour(currentHour);
        timePicker.setMinute(currentMinute);

        TextView[] dayTextViews = new TextView[] {
                alarmCard.findViewById(R.id.alarmPillDateMon),
                alarmCard.findViewById(R.id.alarmPillDateTue),
                alarmCard.findViewById(R.id.alarmPillDateWed),
                alarmCard.findViewById(R.id.alarmPillDateThu),
                alarmCard.findViewById(R.id.alarmPillDateFri),
                alarmCard.findViewById(R.id.alarmPillDateSat),
                alarmCard.findViewById(R.id.alarmPillDateSun)
        };

        TextView everyDayTextView = alarmCard.findViewById(R.id.alarmPillEveryday);

        boolean allChecked = true;
        for (int i = 0; i < dayTextViews.length; i++) {
            boolean isChecked = dayTextViews[i].getVisibility() == View.VISIBLE && dayTextViews[i].getTypeface() != null && dayTextViews[i].getTypeface().isBold();
            dayCheckBoxes[i].setChecked(isChecked);
            if (!isChecked) {
                allChecked = false;
            }
        }

        if (everyDayTextView.getVisibility() == View.VISIBLE) {
            allChecked = true;
            for (CheckBox checkBox : dayCheckBoxes) {
                checkBox.setChecked(true);
            }
        }

        builder.setPositiveButton("확인", (dialog, which) -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            alarmTime.setText(String.format("%02d:%02d", hour, minute));
            alarmPillName.setText(selectedItem);

            boolean allCheckedAfterEdit = true;
            for (CheckBox checkBox : dayCheckBoxes) {
                if (!checkBox.isChecked()) {
                    allCheckedAfterEdit = false;
                    break;
                }
            }

            if (allCheckedAfterEdit) {
                for (TextView dayTextView : dayTextViews) {
                    updateDayTextView(dayTextView, true);
                }
                everyDayTextView.setVisibility(View.VISIBLE);
            } else {
                for (int i = 0; i < dayCheckBoxes.length; i++) {
                    updateDayTextView(dayTextViews[i], dayCheckBoxes[i].isChecked());
                    dayTextViews[i].setVisibility(View.VISIBLE);  // 텍스트뷰를 다시 보이도록 설정
                }
                everyDayTextView.setVisibility(View.INVISIBLE);
            }

            saveAlarmToPreferences(alarmCard);  // Save the updated alarm to SharedPreferences
        });

        builder.setNegativeButton("취소", null);
        builder.create().show();
    }

    private void updateDayTextView(TextView textView, boolean isChecked) {
        if (isChecked) {
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        } else {
            textView.setTypeface(null, Typeface.NORMAL);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }
    }

    // 알람카드 생성 부분에 수정 버튼 이벤트 추가
    private void addAlarmCardEventListeners(LinearLayout alarmCard) {
        Button deleteButton = alarmCard.findViewById(R.id.alarmDelete);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(alarmCard));

        Button modifyButton = alarmCard.findViewById(R.id.alarmModify);
        modifyButton.setOnClickListener(v -> showEditTimeDayPickerDialog(alarmCard));

        Switch alarmSwitch = alarmCard.findViewById(R.id.alarmPillSwitch);
        alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveAlarmToPreferences(alarmCard);
        });
    }

    private void showDeleteConfirmationDialog(final View alarmCard) {
        new AlertDialog.Builder(getContext())
                .setMessage("삭제 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alarmContainer.removeView(alarmCard);
                        removeAlarmFromPreferences((LinearLayout) alarmCard); // LinearLayout로 캐스팅
                    }
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    private void saveAlarmToPreferences(LinearLayout alarmCard) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        TextView alarmTime = alarmCard.findViewById(R.id.alarmPillDateTime);
        TextView alarmPillName = alarmCard.findViewById(R.id.alarmPillName);
        Switch alarmSwitch = alarmCard.findViewById(R.id.alarmPillSwitch);

        String key = alarmTime.getText().toString();
        editor.putString(key + "_name", alarmPillName.getText().toString());
        editor.putBoolean(key + "_switch", alarmSwitch.isChecked());

        TextView[] dayTextViews = new TextView[] {
                alarmCard.findViewById(R.id.alarmPillDateMon),
                alarmCard.findViewById(R.id.alarmPillDateTue),
                alarmCard.findViewById(R.id.alarmPillDateWed),
                alarmCard.findViewById(R.id.alarmPillDateThu),
                alarmCard.findViewById(R.id.alarmPillDateFri),
                alarmCard.findViewById(R.id.alarmPillDateSat),
                alarmCard.findViewById(R.id.alarmPillDateSun)
        };

        for (int i = 0; i < dayTextViews.length; i++) {
            editor.putBoolean(key + "_day" + i, dayTextViews[i].getTypeface() != null && dayTextViews[i].getTypeface().isBold());
        }

        editor.apply();
    }

    private void removeAlarmFromPreferences(LinearLayout alarmCard) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        TextView alarmTime = alarmCard.findViewById(R.id.alarmPillDateTime);
        String key = alarmTime.getText().toString();

        editor.remove(key + "_name");
        editor.remove(key + "_switch");

        TextView[] dayTextViews = new TextView[] {
                alarmCard.findViewById(R.id.alarmPillDateMon),
                alarmCard.findViewById(R.id.alarmPillDateTue),
                alarmCard.findViewById(R.id.alarmPillDateWed),
                alarmCard.findViewById(R.id.alarmPillDateThu),
                alarmCard.findViewById(R.id.alarmPillDateFri),
                alarmCard.findViewById(R.id.alarmPillDateSat),
                alarmCard.findViewById(R.id.alarmPillDateSun)
        };

        for (int i = 0; i < dayTextViews.length; i++) {
            editor.remove(key + "_day" + i);
        }

        editor.apply();
    }

    private void loadAlarmsFromPreferences() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AlarmPreferences", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_name")) {
                String timeKey = key.replace("_name", "");
                String name = (String) entry.getValue();
                boolean switchState = sharedPreferences.getBoolean(timeKey + "_switch", false);

                LinearLayout newAlarmCard = (LinearLayout) inflater.inflate(R.layout.alarm, null);

                TextView alarmTime = newAlarmCard.findViewById(R.id.alarmPillDateTime);
                alarmTime.setText(timeKey);
                alarmTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);

                TextView alarmPillName = newAlarmCard.findViewById(R.id.alarmPillName);
                alarmPillName.setText(name);

                Switch alarmSwitch = newAlarmCard.findViewById(R.id.alarmPillSwitch);
                alarmSwitch.setChecked(switchState);

                TextView[] dayTextViews = new TextView[] {
                        newAlarmCard.findViewById(R.id.alarmPillDateMon),
                        newAlarmCard.findViewById(R.id.alarmPillDateTue),
                        newAlarmCard.findViewById(R.id.alarmPillDateWed),
                        newAlarmCard.findViewById(R.id.alarmPillDateThu),
                        newAlarmCard.findViewById(R.id.alarmPillDateFri),
                        newAlarmCard.findViewById(R.id.alarmPillDateSat),
                        newAlarmCard.findViewById(R.id.alarmPillDateSun)
                };

                boolean allChecked = true;
                for (int i = 0; i < dayTextViews.length; i++) {
                    boolean isChecked = sharedPreferences.getBoolean(timeKey + "_day" + i, false);
                    updateDayTextView(dayTextViews[i], isChecked);
                    if (!isChecked) {
                        allChecked = false;
                    }
                }

                TextView everyDayTextView = newAlarmCard.findViewById(R.id.alarmPillEveryday);
                if (allChecked) {
                    everyDayTextView.setVisibility(View.VISIBLE);
                } else {
                    everyDayTextView.setVisibility(View.INVISIBLE);
                }

                alarmContainer.addView(newAlarmCard);
                addAlarmCardEventListeners(newAlarmCard);
            }
        }
    }


    private CardView makePillCard(Context context, String imageUrl, String medicineName, String medicineCount, String medicineEffect) {
        // 카드 뷰 생성
        CardView pillCard = new CardView(getContext());

        CardView.LayoutParams pillCardParams = new CardView.LayoutParams(
                CardView.LayoutParams.WRAP_CONTENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        // 좌우 마진 설정
        pillCardParams.setMargins(1, 50, 100, 50); // 여기서 16은 원하는 간격 값입니다.
        pillCard.setLayoutParams(pillCardParams);

        // 카드 모서리 변경 (관련 메소드가 존재하지 않아 수정 필요)
        // pillCard.setCardCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.card_corner_radius));

        // 카드 모서리와 그림자 설정
        pillCard.setRadius(context.getResources().getDimension(R.dimen.card_corner_radius));
        pillCard.setCardElevation(context.getResources().getDimension(R.dimen.card_elevation));

        // 터치 효과를 위해 배경 설정
        pillCard.setClickable(true);
        pillCard.setFocusable(true);

        pillCard.setCardElevation(context.getResources().getDimensionPixelSize(R.dimen.card_elevation));

        // 카드 안의 content를 위한 리니어레이아웃 생성
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        linearLayout.setPadding(40, 40, 40, 40); // 내부 패딩 설정

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // 카드 썸네일을 위한 이미지뷰 생성
        ImageView imageView = new ImageView(context);
        Log.d("MainActivity", imageUrl);
        // url 통해서 이미지 가져오기
        // 이미지뷰에 이미지 설정
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 이미지가 있는 경우 Glide를 사용하여 이미지를 설정합니다.
            Glide.with(context)
                    .load(imageUrl)
                    .error(R.drawable.no_image) // 이미지 로드 중 오류가 발생할 경우 no_image 리소스를 표시합니다.
                    .into(imageView);
        } else {
            // 이미지가 없는 경우 drawable 리소스에서 no_image를 사용합니다.
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_image));
        }

        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, 400);

        imageView.setLayoutParams(layoutParams);

        // 이미지 경로 설정
        //imageView.setImageDrawable(imageDrawable);
        // 이미지 alt 설정
        imageView.setContentDescription(context.getString(R.string.card_content_description));

        // 약품명 TextView 생성
        TextView nameView = new TextView(context);
        nameView.setId(View.generateViewId());
        // ConstraintLayout.LayoutParams로 변경
        ConstraintLayout.LayoutParams nameViewParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );

        nameViewParams.width = 300; // 폭을 400픽셀로 설정
        nameView.setLayoutParams(nameViewParams);
        nameView.setText(medicineName);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        nameView.setMaxLines(1); // 한 줄만 표기되도록
        nameView.setEllipsize(TextUtils.TruncateAt.END); // 한 줄 넘어가면 ...으로 생략
        nameView.setTypeface(null, Typeface.BOLD);

        // 약품 복용 횟수 TextView 생성
        TextView countView = new TextView(context);
        countView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams countViewParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        countViewParams.width = 300; // 폭을 300픽셀로 설정
        countView.setLayoutParams(countViewParams);
        countView.setText(medicineCount);
        countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        countView.setMaxLines(1);
        countView.setEllipsize(TextUtils.TruncateAt.END);
        countView.setTypeface(null, Typeface.BOLD);

        // 약품 효능 TextView 생성
        TextView effectView = new TextView(context);
        effectView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams effectViewParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        effectViewParams.width = 300; // 폭을 200픽셀로 설정
        effectView.setLayoutParams(effectViewParams);
        effectView.setText(medicineEffect);
        effectView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        effectView.setMaxLines(1);
        effectView.setEllipsize(TextUtils.TruncateAt.END);
        effectView.setTypeface(null, Typeface.BOLD);

        // 카드 뷰에 내용 추가
        linearLayout.addView(imageView);
        linearLayout.addView(nameView);
        linearLayout.addView(effectView); // 이 부분을 medicineCount 밑으로 이동하여 수정하였습니다.
        pillCard.addView(linearLayout); // linearLayout을 카드에 추가

        // 상세 페이지로 넘어가기 위한 클릭 리스너 구현
        pillCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Intent를 통해 상세페이지에 약의 정보들을 전달
                Intent intent = new Intent(context, DrugDetailActivity.class);
                intent.putExtra("medicineName", medicineName);
                intent.putExtra("medicineCount", medicineCount);
                intent.putExtra("medicineEffect", medicineEffect);
                intent.putExtra("medicineImage", imageUrl); // Example drawable resource ID
                context.startActivity(intent);
            }
        });

        return pillCard;
    }
}