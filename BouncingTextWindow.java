import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Random;

public class BouncingTextWindow {
    // Constants for default image and text behavior
    static final int TEXT_SPEED = 2;
    static final int TEXT_SIZE = 20;
    static int imageWidth = 640;  // Default image width
    static int imageHeight = 480; // Default image height
    private static boolean fullscreenKeybindEnabled = false; // Toggle fullscreen keybind
    private static boolean isFullscreen = false; // Track fullscreen state
    static boolean isPartyMode = false;  // Track party mode state
    private static JFrame frame;

    public static void main(String[] args) {
        // Check if Party Mode is enabled via command-line arguments
        for (String arg : args) {
            if (arg.equals("-pm") || arg.equals("--party")) {
                isPartyMode = true;
                System.out.println("Party Mode Enabled!");
            }
        }

        // Create the main application window with title "DVD Logo"
        frame = new JFrame("DVD Logo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Adjust frame size to accommodate content

        BouncingTextPanel bouncingTextPanel = new BouncingTextPanel();

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create "File" menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        // Create "Upload Image" menu item
        JMenuItem uploadImageItem = new JMenuItem("Upload Image");
        uploadImageItem.addActionListener(e -> bouncingTextPanel.uploadImage());
        fileMenu.add(uploadImageItem);

        // Create "Options" menu item for resizing the image and fullscreen keybind
        JMenuItem optionsItem = new JMenuItem("Options");
        optionsItem.addActionListener(e -> showOptionsDialog(bouncingTextPanel));
        fileMenu.add(optionsItem);

        frame.setJMenuBar(menuBar);
        frame.add(bouncingTextPanel);
        frame.setVisible(true);

        // Add a key listener for toggling fullscreen mode with F11
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (fullscreenKeybindEnabled && e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullscreen();
                }
            }
        });
    }

    /**
     * Displays the options dialog where the user can select different image sizes and enable/disable the fullscreen keybind.
     */
    private static void showOptionsDialog(BouncingTextPanel panel) {
        // Create a dialog for the options
        JDialog optionsDialog = new JDialog(frame, "Options", true);
        optionsDialog.setLayout(new FlowLayout());
        optionsDialog.setSize(300, 200);

        // Create radio buttons for different scaling options
        JRadioButton size1 = new JRadioButton("320x240");
        JRadioButton size2 = new JRadioButton("640x480", true); // Default size selected
        JRadioButton size3 = new JRadioButton("800x600");

        // Group the radio buttons so only one can be selected
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(size1);
        sizeGroup.add(size2);
        sizeGroup.add(size3);

        // Create a checkbox for enabling/disabling fullscreen keybind
        JCheckBox fullscreenKeybindCheckbox = new JCheckBox("Enable F11 Fullscreen Keybind", fullscreenKeybindEnabled);
        fullscreenKeybindCheckbox.addActionListener(e -> fullscreenKeybindEnabled = fullscreenKeybindCheckbox.isSelected());

        // Add the buttons and checkbox to the dialog
        optionsDialog.add(size1);
        optionsDialog.add(size2);
        optionsDialog.add(size3);
        optionsDialog.add(fullscreenKeybindCheckbox);

        // Create an "Apply" button to apply the selected size and fullscreen keybind setting
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            // Update the image size based on the selected option
            if (size1.isSelected()) {
                imageWidth = 320;
                imageHeight = 240;
            } else if (size2.isSelected()) {
                imageWidth = 640;
                imageHeight = 480;
            } else if (size3.isSelected()) {
                imageWidth = 800;
                imageHeight = 600;
            }

            // Notify the panel to repaint with the new image size
            panel.updateImageSize(imageWidth, imageHeight);
            optionsDialog.dispose();
        });

        // Add the "Apply" button to the dialog
        optionsDialog.add(applyButton);

        // Display the dialog
        optionsDialog.setVisible(true);
    }

    /**
     * Toggles fullscreen mode (borderless) on and off when F11 is pressed.
     */
    private static void toggleFullscreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (isFullscreen) {
            // Exit fullscreen: restore window decorations and size
            frame.dispose();
            frame.setUndecorated(false);
            frame.setVisible(true);
            device.setFullScreenWindow(null);
            frame.setSize(800, 600); // Restore to initial size
        } else {
            // Enter fullscreen: remove window borders and go borderless fullscreen
            frame.dispose(); // Dispose first to change undecorated setting
            frame.setUndecorated(true); // Remove title bar and borders
            frame.setVisible(true); // Set it visible again
            device.setFullScreenWindow(frame); // Set frame to fullscreen
        }
        isFullscreen = !isFullscreen; // Toggle fullscreen state
    }
}

class BouncingTextPanel extends JPanel implements ActionListener {
    private Timer timer;
    private int textX = 0;
    private int textY = 50;
    private int textDX = BouncingTextWindow.TEXT_SPEED; // Speed for x direction
    private int textDY = BouncingTextWindow.TEXT_SPEED; // Speed for y direction
    private String text = "DVD Logo";
    private BufferedImage image = null;
    private Random random = new Random();

    public BouncingTextPanel() {
        // Set the background color to black initially
        setBackground(Color.BLACK);
        timer = new Timer(10, this); // Update every 10 milliseconds
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            // Draw the resized image at the current position
            g.drawImage(image, textX, textY, BouncingTextWindow.imageWidth, BouncingTextWindow.imageHeight, this);
        } else {
            // Draw the text if no image is available
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, BouncingTextWindow.TEXT_SIZE));
            g.drawString(text, textX, textY);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update text/image position
        textX += textDX;
        textY += textDY;

        if (image != null) {
            // Bounce off edges for the image
            if (textX < 0 || textX > getWidth() - BouncingTextWindow.imageWidth) {
                textDX = -textDX;
            }
            if (textY < 0 || textY > getHeight() - BouncingTextWindow.imageHeight) {
                textDY = -textDY;
            }
        } else {
            // Bounce off edges for the text
            if (textX < 0 || textX > getWidth() - getFontMetrics(getFont()).stringWidth(text)) {
                textDX = -textDX;
            }
            if (textY < BouncingTextWindow.TEXT_SIZE || textY > getHeight()) {
                textDY = -textDY;
            }
        }

        // If Party Mode is enabled, change the background color randomly
        if (BouncingTextWindow.isPartyMode) {
            setBackground(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }

        repaint();
    }

    /**
     * Opens a file chooser to allow the user to upload an image.
     */
    public void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedImage originalImage = ImageIO.read(file);
                if (originalImage == null) {
                    throw new IllegalArgumentException("The selected file is not a valid image.");
                }
                image = originalImage;
                updateImageSize(BouncingTextWindow.imageWidth, BouncingTextWindow.imageHeight);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Updates the image size when the user selects a new size from the options dialog.
     * 
     * @param width  New width of the image
     * @param height New height of the image
     */
    public void updateImageSize(int width, int height) {
        if (image != null) {
            // Resize the image to the new size
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(image, 0, 0, width, height, null);
            g2d.dispose();
            image = resizedImage;
            repaint();
        }
    }
}
