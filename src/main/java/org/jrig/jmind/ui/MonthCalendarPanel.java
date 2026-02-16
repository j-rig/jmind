/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

public class MonthCalendarPanel extends JPanel {
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private Set<LocalDate> datesWithEntries;
    private List<DateSelectionListener> listeners;

    private JLabel monthYearLabel;
    private JPanel calendarGrid;

    private static final Color SELECTED_COLOR = new Color(100, 149, 237);
    private static final Color HAS_ENTRY_COLOR = new Color(144, 238, 144);
    private static final Color TODAY_COLOR = new Color(255, 218, 185);
    private static final Color HOVER_COLOR = new Color(230, 230, 250);

    public interface DateSelectionListener {
        void dateSelected(LocalDate date);
    }

    public MonthCalendarPanel() {
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        this.datesWithEntries = new HashSet<>();
        this.listeners = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCalendarPanel(), BorderLayout.CENTER);

        refreshCalendar();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton prevButton = new JButton("◄");
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton nextButton = new JButton("►");
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            currentMonth = YearMonth.now();
            selectedDate = LocalDate.now();
            refreshCalendar();
            notifyListeners(selectedDate);
        });

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevButton);
        navPanel.add(monthYearLabel);
        navPanel.add(nextButton);

        panel.add(navPanel, BorderLayout.CENTER);
        panel.add(todayButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Day headers
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (String day : dayNames) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            headerPanel.add(label);
        }

        // Calendar grid
        calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(calendarGrid, BorderLayout.CENTER);

        return panel;
    }

    private void refreshCalendar() {
        calendarGrid.removeAll();

        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        monthYearLabel.setText(monthName + " " + currentMonth.getYear());

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = currentMonth.lengthOfMonth();

        // Add empty cells for days before the first of the month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Add day buttons
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayButton = createDayButton(date, today);
            calendarGrid.add(dayButton);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JButton createDayButton(LocalDate date, LocalDate today) {
        JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 5, 5, 5));

        // Determine background color
        Color bgColor = Color.WHITE;
        if (date.equals(selectedDate)) {
            bgColor = SELECTED_COLOR;
            button.setForeground(Color.WHITE);
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        } else if (datesWithEntries.contains(date)) {
            bgColor = HAS_ENTRY_COLOR;
        } else if (date.equals(today)) {
            bgColor = TODAY_COLOR;
        }

        button.setBackground(bgColor);
        button.setOpaque(true);

        // Add hover effect
        final Color finalBgColor = bgColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!date.equals(selectedDate)) {
                    button.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(finalBgColor);
            }
        });

        button.addActionListener(e -> {
            selectedDate = date;
            refreshCalendar();
            notifyListeners(date);
        });

        return button;
    }

    public void setDatesWithEntries(Set<LocalDate> dates) {
        this.datesWithEntries = dates;
        refreshCalendar();
    }

    public void addDateSelectionListener(DateSelectionListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(LocalDate date) {
        for (DateSelectionListener listener : listeners) {
            listener.dateSelected(date);
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }
}