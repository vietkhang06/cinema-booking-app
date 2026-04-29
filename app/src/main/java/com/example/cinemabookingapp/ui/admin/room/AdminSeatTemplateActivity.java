package com.example.cinemabookingapp.ui.admin.room;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanCell;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanRow;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanRowAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSeatTemplateActivity extends AppCompatActivity {

    private static final int DEFAULT_ROWS = 6;
    private static final int DEFAULT_COLUMNS = 12;

    private final List<SeatPlanRow> seatRows = new ArrayList<>();

    private SeatPlanRowAdapter rowAdapter;
    private int selectedPaintType = SeatPlanCell.TYPE_NORMAL;

    private TextView tvCurrentMode;
    private TextView tvSummary;
    private EditText etRows;
    private EditText etColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seat_template);

        RecyclerView rvSeatRows = findViewById(R.id.rvSeatRows);
        tvCurrentMode = findViewById(R.id.tvSeatCurrentMode);
        tvSummary = findViewById(R.id.tvSeatSummary);
        etRows = findViewById(R.id.etSeatRows);
        etColumns = findViewById(R.id.etSeatColumns);

        Button btnModeNormal = findViewById(R.id.btnModeNormal);
        Button btnModeVip = findViewById(R.id.btnModeVip);
        Button btnModeCouple = findViewById(R.id.btnModeCouple);
        Button btnModeLocked = findViewById(R.id.btnModeLocked);
        Button btnGenerate = findViewById(R.id.btnGenerateSeatPlan);
        Button btnReset = findViewById(R.id.btnResetSeatPlan);
        Button btnSave = findViewById(R.id.btnSaveSeatPlan);

        rvSeatRows.setLayoutManager(new LinearLayoutManager(this));
        rvSeatRows.setHasFixedSize(true);

        rowAdapter = new SeatPlanRowAdapter(seatRows, (rowPosition, seatPosition) -> {
            SeatPlanCell cell = seatRows.get(rowPosition).cells.get(seatPosition);
            cell.type = selectedPaintType;
            rowAdapter.notifyItemChanged(rowPosition);
            updateSummary();
        });

        rvSeatRows.setAdapter(rowAdapter);

        btnModeNormal.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_NORMAL));
        btnModeVip.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_VIP));
        btnModeCouple.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_COUPLE));
        btnModeLocked.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_LOCKED));

        btnGenerate.setOnClickListener(v -> generateFromInputs());
        btnReset.setOnClickListener(v -> resetTemplate());
        btnSave.setOnClickListener(v -> {
            updateSummary();
            Toast.makeText(this, "Đã lưu mẫu ghế (demo)", Toast.LENGTH_SHORT).show();
        });

        setPaintMode(SeatPlanCell.TYPE_NORMAL);
        generateTemplate(DEFAULT_ROWS, DEFAULT_COLUMNS);
    }

    private void setPaintMode(int type) {
        selectedPaintType = type;
        tvCurrentMode.setText("Chế độ hiện tại: " + modeName(type));
    }

    private void generateFromInputs() {
        int rows = parsePositiveInt(etRows.getText().toString(), DEFAULT_ROWS);
        int cols = parsePositiveInt(etColumns.getText().toString(), DEFAULT_COLUMNS);

        if (rows < 1) rows = DEFAULT_ROWS;
        if (cols < 1) cols = DEFAULT_COLUMNS;

        if (rows > 26) rows = 26;
        if (cols > 20) cols = 20;

        generateTemplate(rows, cols);
        Toast.makeText(this, "Đã tạo sơ đồ ghế " + rows + " x " + cols, Toast.LENGTH_SHORT).show();
    }

    private void resetTemplate() {
        etRows.setText(String.valueOf(DEFAULT_ROWS));
        etColumns.setText(String.valueOf(DEFAULT_COLUMNS));
        setPaintMode(SeatPlanCell.TYPE_NORMAL);
        generateTemplate(DEFAULT_ROWS, DEFAULT_COLUMNS);
        Toast.makeText(this, "Đã reset sơ đồ ghế", Toast.LENGTH_SHORT).show();
    }

    private void generateTemplate(int rowCount, int columnCount) {
        seatRows.clear();

        for (int r = 0; r < rowCount; r++) {
            String rowName = String.valueOf((char) ('A' + r));
            List<SeatPlanCell> cells = new ArrayList<>();

            for (int c = columnCount; c >= 1; c--) {
                String seatCode = rowName + String.format(Locale.getDefault(), "%02d", c);

                int type = SeatPlanCell.TYPE_NORMAL;

                // Tạo điểm nhấn giống kiểu bạn gửi: 2 ghế đầu hàng A là VIP
                if (r == 0 && (c == columnCount || c == columnCount - 1)) {
                    type = SeatPlanCell.TYPE_VIP;
                }

                cells.add(new SeatPlanCell(seatCode, type));
            }

            seatRows.add(new SeatPlanRow(rowName, cells));
        }

        rowAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        int normal = 0;
        int vip = 0;
        int couple = 0;
        int locked = 0;

        for (SeatPlanRow row : seatRows) {
            for (SeatPlanCell cell : row.cells) {
                switch (cell.type) {
                    case SeatPlanCell.TYPE_VIP:
                        vip++;
                        break;
                    case SeatPlanCell.TYPE_COUPLE:
                        couple++;
                        break;
                    case SeatPlanCell.TYPE_LOCKED:
                        locked++;
                        break;
                    default:
                        normal++;
                        break;
                }
            }
        }

        tvSummary.setText("Normal: " + normal +
                " | VIP: " + vip +
                " | Couple: " + couple +
                " | Locked: " + locked);
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int result = Integer.parseInt(value.trim());
            return result > 0 ? result : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String modeName(int type) {
        switch (type) {
            case SeatPlanCell.TYPE_VIP:
                return "VIP";
            case SeatPlanCell.TYPE_COUPLE:
                return "COUPLE";
            case SeatPlanCell.TYPE_LOCKED:
                return "LOCKED";
            default:
                return "NORMAL";
        }
    }
}