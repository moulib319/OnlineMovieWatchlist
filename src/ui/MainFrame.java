package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import model.Movie;
import model.Watchlist;
import storage.FileStorage;

public class MainFrame extends JFrame {

    private final Watchlist watchlist;
    private final File dataFile;
    private final ArrayList<Movie> visibleMovies = new ArrayList<Movie>();

    private JTextField titleField;
    private JTextField genreField;
    private JTextArea descriptionArea;
    private JTextField searchField;
    private JComboBox<String> genreFilter;
    private JComboBox<String> statusFilter;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel statusLabel;

    private boolean adjustingFilters = false;

    public MainFrame() {
        super("Online Movie Watchlist");
        dataFile = new File("data" + File.separator + "watchlist.txt");
        watchlist = FileStorage.load(dataFile);
        buildUI();
        refreshTable();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveAndExit();
            }
        });
        setSize(920, 620);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(buildInputPanel());
        top.add(buildFilterPanel());
        root.add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[] { "Title", "Genre", "Description", "Status" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    String status = String.valueOf(getValueAt(row, 3));
                    if ("Watched".equals(status)) {
                        c.setForeground(new Color(120, 120, 120));
                        c.setFont(getFont().deriveFont(Font.ITALIC));
                    } else {
                        c.setForeground(Color.BLACK);
                        c.setFont(getFont());
                    }
                }
                return c;
            }
        };
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        root.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add Movie"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        titleField = new JTextField(20);
        genreField = new JTextField(14);
        descriptionArea = new JTextArea(2, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        panel.add(titleField, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Genre:"), gbc);

        gbc.gridx = 3;
        panel.add(genreField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(new JScrollPane(descriptionArea), gbc);
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JButton addButton = new JButton("Add Movie");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMovie();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(addButton, gbc);

        return panel;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        searchField = new JTextField(18);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!adjustingFilters) {
                    refreshTable();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!adjustingFilters) {
                    refreshTable();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!adjustingFilters) {
                    refreshTable();
                }
            }
        });

        genreFilter = new JComboBox<String>(new String[] { "All" });
        genreFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!adjustingFilters) {
                    refreshTable();
                }
            }
        });

        statusFilter = new JComboBox<String>(
                new String[] { "All", "Watched", "Not Watched" });
        statusFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!adjustingFilters) {
                    refreshTable();
                }
            }
        });

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFilters();
            }
        });

        panel.add(new JLabel("Search title:"));
        panel.add(searchField);
        panel.add(new JLabel("Genre:"));
        panel.add(genreFilter);
        panel.add(new JLabel("Status:"));
        panel.add(statusFilter);
        panel.add(clearButton);

        return panel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton toggleButton = new JButton("Toggle Watched");
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSelected();
            }
        });

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelected();
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveWatchlist();
            }
        });

        buttons.add(toggleButton);
        buttons.add(removeButton);
        buttons.add(saveButton);

        statusLabel = new JLabel(" ");
        panel.add(buttons, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private void addMovie() {
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Title cannot be empty.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (watchlist.containsTitle(title)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "A movie titled \"" + title + "\" already exists. Add anyway?",
                    "Duplicate Title", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        String finalGenre = genre.length() == 0 ? "Uncategorized" : genre;
        watchlist.addMovie(new Movie(title, finalGenre, description));

        titleField.setText("");
        genreField.setText("");
        descriptionArea.setText("");

        refreshTable();
        statusLabel.setText("Added \"" + title + "\".");
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a movie in the table first.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Movie movie = visibleMovies.get(row);
        watchlist.toggleWatched(movie);
        String state = movie.isWatched() ? "Watched" : "Not Watched";
        refreshTable();
        statusLabel.setText("\"" + movie.getTitle() + "\" marked as " + state + ".");
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a movie in the table first.", "No Selection",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Movie movie = visibleMovies.get(row);
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove \"" + movie.getTitle() + "\" from your watchlist?",
                "Remove Movie", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        watchlist.removeMovie(movie);
        refreshTable();
        statusLabel.setText("Removed \"" + movie.getTitle() + "\".");
    }

    private void clearFilters() {
        adjustingFilters = true;
        searchField.setText("");
        genreFilter.setSelectedItem("All");
        statusFilter.setSelectedItem("All");
        adjustingFilters = false;
        refreshTable();
    }

    private void refreshTable() {
        String genre = (String) genreFilter.getSelectedItem();
        String status = (String) statusFilter.getSelectedItem();
        String keyword = searchField.getText();

        visibleMovies.clear();
        visibleMovies.addAll(watchlist.filter(genre, status, keyword));

        tableModel.setRowCount(0);
        for (int i = 0; i < visibleMovies.size(); i++) {
            Movie movie = visibleMovies.get(i);
            tableModel.addRow(new Object[] {
                    movie.getTitle(),
                    movie.getGenre(),
                    movie.getDescription(),
                    movie.isWatched() ? "Watched" : "Not Watched"
            });
        }

        updateGenreFilter();
    }

    private void updateGenreFilter() {
        adjustingFilters = true;
        Object current = genreFilter.getSelectedItem();
        genreFilter.removeAllItems();
        genreFilter.addItem("All");

        ArrayList<String> genres = watchlist.getGenres();
        for (int i = 0; i < genres.size(); i++) {
            genreFilter.addItem(genres.get(i));
        }

        boolean restored = false;
        if (current != null) {
            for (int i = 0; i < genreFilter.getItemCount(); i++) {
                if (genreFilter.getItemAt(i).equals(current)) {
                    genreFilter.setSelectedIndex(i);
                    restored = true;
                    break;
                }
            }
        }
        if (!restored) {
            genreFilter.setSelectedItem("All");
        }
        adjustingFilters = false;
    }

    private void saveWatchlist() {
        try {
            FileStorage.save(watchlist, dataFile);
            statusLabel.setText("Saved " + watchlist.size()
                    + " movie(s) to " + dataFile.getPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not save watchlist: " + e.getMessage(), "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndExit() {
        try {
            FileStorage.save(watchlist, dataFile);
        } catch (IOException e) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Could not save the watchlist: " + e.getMessage()
                            + "\nExit anyway?",
                    "Save Error", JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }
}
