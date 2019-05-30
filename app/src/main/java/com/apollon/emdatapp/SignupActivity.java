package com.apollon.emdatapp;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.apollon.emdatapp.Model.Address;
import com.apollon.emdatapp.Model.City;
import com.apollon.emdatapp.Model.Country;
import com.apollon.emdatapp.Model.Province;
import com.apollon.emdatapp.Model.Region;
import com.apollon.emdatapp.Model.Report;
import com.apollon.emdatapp.Model.Smartphone;
import com.apollon.emdatapp.Model.User;
import com.apollon.emdatapp.Service.GeoService;
import com.apollon.emdatapp.Service.UserService;
import com.apollon.emdatapp.Service.VolleyCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private EditText inputName, inputSurname, inputSsn, inputDob, inputPhone, inputAddressName, inputStreetNumber, inputEmail, inputPassword;
    private Button btnSignUp, btnResetPassword;
    private Spinner countrySpinner, regionSpinner, provinceSpinner, citySpinner;
    private TextView imei, produttore, modello;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private TelephonyManager telephonyManager;
    private String deviceIMEI, manufacturer, model;
    GeoService geoService;
    UserService userService;

    private Calendar myCalendar;
    private String date_birth;

    ArrayList<Country> countries = new ArrayList<Country>();
    Country selectedCountry;
    ArrayList<Region> regions = new ArrayList<Region>();
    Region selectedRegion;
    ArrayList<Province> provinces = new ArrayList<Province>();
    Province selectedProvince;
    ArrayList<City> cities = new ArrayList<City>();
    City selectedCity;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            if (intent.getStringExtra("type").equals("countries")) {
                ArrayList<String> countriesName = new ArrayList<String>();

                try {
                    String reportString = intent.getStringExtra("countries");
                    JSONObject json = new JSONObject(reportString);
                    JSONArray jsonCountries = json.getJSONArray("geonames");

                    for (int i = 0; i < jsonCountries.length(); i++) {
                        JSONObject jsoncountry = jsonCountries.getJSONObject(i);
                        Country country = new Country();
                        country.setId((Integer) jsoncountry.get("geonameId"));
                        country.setName((String) jsoncountry.get("countryName"));
                        countries.add(country);
                        countriesName.add(country.getName());
                    }
                    Collections.sort(countriesName, String.CASE_INSENSITIVE_ORDER);
                    countriesName.add(0, "Scegli una Nazione");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, countriesName);
                    countrySpinner.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getStringExtra("type").equals("children1")) {
                ArrayList<String> regionsName = new ArrayList<String>();

                try {
                    String reportString = intent.getStringExtra("children1");
                    JSONObject json = new JSONObject(reportString);
                    JSONArray jsonRegions = json.getJSONArray("geonames");

                    for (int i = 0; i < jsonRegions.length(); i++) {
                        JSONObject jsonregion = jsonRegions.getJSONObject(i);
                        Region region = new Region();
                        region.setId((Integer) jsonregion.get("geonameId"));
                        region.setName((String) jsonregion.get("toponymName"));
                        regions.add(region);
                        regionsName.add(region.getName());
                    }
                    Collections.sort(regionsName, String.CASE_INSENSITIVE_ORDER);
                    regionsName.add(0, "Scegli una Regione");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, regionsName);
                    regionSpinner.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getStringExtra("type").equals("children2")) {
                ArrayList<String> provincesName = new ArrayList<String>();

                try {
                    String reportString = intent.getStringExtra("children2");
                    JSONObject json = new JSONObject(reportString);
                    JSONArray jsonProvinces = json.getJSONArray("geonames");

                    for (int i = 0; i < jsonProvinces.length(); i++) {
                        JSONObject jsonprovince = jsonProvinces.getJSONObject(i);
                        Province province = new Province();
                        province.setId((Integer) jsonprovince.get("geonameId"));
                        province.setName((String) jsonprovince.get("toponymName"));
                        provinces.add(province);
                        provincesName.add(province.getName());
                    }
                    Collections.sort(provincesName, String.CASE_INSENSITIVE_ORDER);
                    provincesName.add(0, "Scegli una Provincia");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, provincesName);
                    provinceSpinner.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getStringExtra("type").equals("children3")) {
                ArrayList<String> citiesName = new ArrayList<String>();

                try {
                    String reportString = intent.getStringExtra("children3");
                    JSONObject json = new JSONObject(reportString);
                    JSONArray jsonCities = json.getJSONArray("geonames");

                    for (int i = 0; i < jsonCities.length(); i++) {
                        JSONObject jsoncity = jsonCities.getJSONObject(i);
                        City city = new City();
                        city.setId((Integer) jsoncity.get("geonameId"));
                        city.setName((String) jsoncity.get("toponymName"));
                        cities.add(city);
                        citiesName.add(city.getName());
                    }
                    Collections.sort(citiesName, String.CASE_INSENSITIVE_ORDER);
                    citiesName.add(0, "Scegli una Città");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, citiesName);
                    citySpinner.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };

    private BroadcastReceiver registerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent

            Toast.makeText(SignupActivity.this, "Registrazione completata con successo! Per favore, controlla la tua email per confermarla",
                    Toast.LENGTH_LONG).show();
            Intent intentBack = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intentBack);
            finish();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        countrySpinner = (Spinner) findViewById(R.id.spinnerCountry);
        regionSpinner = (Spinner) findViewById(R.id.spinnerRegion);
        provinceSpinner = (Spinner) findViewById(R.id.spinnerProvince);
        citySpinner = (Spinner) findViewById(R.id.spinnerCity);
        inputName = (EditText) findViewById(R.id.name);
        inputSurname = (EditText) findViewById(R.id.surname);
        inputName = (EditText) findViewById(R.id.name);
        inputSsn = (EditText) findViewById(R.id.ssn);
        inputPhone = (EditText) findViewById(R.id.phone);
        inputAddressName = (EditText) findViewById(R.id.addressname);
        inputStreetNumber = (EditText) findViewById(R.id.streetnumber);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        inputDob = (EditText) findViewById(R.id.dob);

        imei = findViewById(R.id.imei);
        produttore = findViewById(R.id.manufacturer);
        modello = findViewById(R.id.model);

        LocalBroadcastManager.getInstance(SignupActivity.this).registerReceiver(
                mMessageReceiver, new IntentFilter("countryUpdates"));

        LocalBroadcastManager.getInstance(SignupActivity.this).registerReceiver(
                registerReceiver, new IntentFilter("registerUser"));

        geoService = new GeoService();
        geoService.getCountries(this);

        ArrayList<String> regionDefault = new ArrayList<String>();
        regionDefault.add("Scegli una Regione");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, regionDefault);
        regionSpinner.setAdapter(adapter);
        ArrayList<String> provinceDefault = new ArrayList<String>();
        provinceDefault.add("Scegli una Provincia");
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, provinceDefault);
        provinceSpinner.setAdapter(adapter2);
        ArrayList<String> cityDefault = new ArrayList<String>();
        cityDefault.add("Scegli una Città");
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cityDefault);
        citySpinner.setAdapter(adapter3);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            deviceIMEI = telephonyManager.getDeviceId();
            manufacturer = Build.MANUFACTURER;
            model = Build.MODEL;
            imei.setText(deviceIMEI);
            produttore.setText(manufacturer);
            modello.setText(model);
        }

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        inputDob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(SignupActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);

                selectedRegion = null;
                selectedProvince = null;
                selectedCity = null;
                for (Country country : countries) {
                    if (parent.getSelectedItem().toString().equals(country.getName())) {
                        selectedCountry = country;
                        break;
                    }
                }
                if(selectedCountry != null) {
                    geoService.getChildren1(getApplicationContext(), selectedCountry.getId());
                    selectedRegion = null;
                    selectedProvince = null;
                    selectedCity = null;
                    //provinces = null;
                    //cities = null;
                    ArrayList<String> provinceDefault = new ArrayList<String>();
                    provinceDefault.add("Scegli una Provincia");
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, provinceDefault);
                    provinceSpinner.setAdapter(adapter2);
                    ArrayList<String> cityDefault = new ArrayList<String>();
                    cityDefault.add("Scegli una Città");
                    ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cityDefault);
                    citySpinner.setAdapter(adapter3);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                selectedProvince = null;
                selectedCity = null;
                for (Region region : regions) {
                    if (parent.getSelectedItem().toString().equals(region.getName())) {
                        selectedRegion = region;
                        break;
                    }
                }
                if (selectedRegion != null) {
                    geoService.getChildren2(getApplicationContext(), selectedRegion.getId());
                    selectedProvince = null;
                    selectedCity = null;
                    //cities = null;
                    ArrayList<String> cityDefault = new ArrayList<String>();
                    cityDefault.add("Scegli una Città");
                    ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, cityDefault);
                    citySpinner.setAdapter(adapter3);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                selectedCity = null;
                if (provinces != null) {
                    for (Province province : provinces) {
                        if (parent.getSelectedItem().toString().equals(province.getName())) {
                            selectedProvince = province;
                            break;
                        }
                    }
                    if (selectedProvince != null) {
                        geoService.getChildren3(getApplicationContext(), selectedProvince.getId());
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                if (cities != null) {
                    for (City city : cities) {
                        if (parent.getSelectedItem().toString().equals(city.getName())) {
                            selectedCity = city;
                            break;
                        }
                    }
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        countrySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SignupActivity.this);
                return false;
            }
        }) ;

        regionSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SignupActivity.this);
                return false;
            }
        }) ;

        provinceSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SignupActivity.this);
                return false;
            }
        }) ;

        citySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(SignupActivity.this);
                return false;
            }
        }) ;

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = inputName.getText().toString().trim();
                final String surname = inputSurname.getText().toString().trim();
                final String ssn = inputSsn.getText().toString().trim();
                final String phone = inputPhone.getText().toString().trim();
                final String streetName = inputAddressName.getText().toString().trim();
                final String homeNumber = inputStreetNumber.getText().toString().trim();
                final String email = inputEmail.getText().toString().trim();
                final String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getApplicationContext(), "Inserisci il tuo Nome!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(surname)) {
                    Toast.makeText(getApplicationContext(), "Inserisci il tuo Cognome!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(ssn)) {
                    Toast.makeText(getApplicationContext(), "Inserisci il tuo Codice Fiscale!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Period.between(myCalendar.getTime().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(), LocalDate.now()).getYears() < 18) {
                    Toast.makeText(getApplicationContext(), "Devi essere maggiorenne per utilizzare il servizio!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(getApplicationContext(), "Inserisci il tuo Numero di Telefono!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedCountry == null || selectedRegion == null || selectedProvince == null || selectedCity == null || TextUtils.isEmpty(streetName) || TextUtils.isEmpty(homeNumber)) {
                    Toast.makeText(getApplicationContext(), "Completa tutti i campi del tuo Indirizzo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Inserisci il tuo Indirizzo Email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Inserisci la tua Password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "La Password è troppo corta. Sono richiesti minimo 6 caratteri!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Registrazione fallita." + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    auth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {

                                                        Address address = new Address();
                                                        address.setCountry(selectedCountry);
                                                        address.setRegion(selectedRegion);
                                                        address.setProvince(selectedProvince);
                                                        address.setCity(selectedCity);
                                                        address.setStreetName(streetName);
                                                        address.setHomeNumber(homeNumber);

                                                        Smartphone smartphone = new Smartphone();
                                                        smartphone.setManufacturer(manufacturer);
                                                        smartphone.setModel(model);
                                                        smartphone.setImei(deviceIMEI);

                                                        User user = new User();
                                                        user.setName(name);
                                                        user.setSurname(surname);
                                                        user.setSsn(ssn);
                                                        user.setDob(date_birth);
                                                        user.setPhoneNumber(phone);
                                                        user.setAddress(address);
                                                        user.setEmail(email);
                                                        user.setSmartphone(smartphone);

                                                        userService = new UserService();
                                                        userService.registerUser(getApplicationContext(), user);


                                                    } else {
                                                        Toast.makeText(SignupActivity.this, task.getException().getMessage(),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    /*startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();*/
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void updateLabel() {
        String SQLFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(SQLFormat, Locale.ITALY);
        date_birth = sdf.format(myCalendar.getTime());
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf2 = new SimpleDateFormat(myFormat, Locale.ITALY);
        inputDob.setText(sdf2.format(myCalendar.getTime()));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
