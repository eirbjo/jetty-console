/*
 * Copyright 2015 Eirik Bjørsnøs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simplericity.jettyconsole;

import org.simplericity.jettyconsole.io.JTextAreaOutputStream;
import org.simplericity.jettyconsole.io.MultiOutputStream;
import org.simplericity.jettyconsole.api.StartOption;
import org.simplericity.jettyconsole.api.Configuration;
import org.simplericity.jettyconsole.api.JettyConsolePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kantega.jexmec.PluginManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.Properties;

/**
 * A graphical console for starting and stopping a webapp in Jetty.
 */
public class JettyConsole {
    private AbstractAction stopAction;
    private AbstractAction startAction;
    private JScrollPane scroll;

    private MultiOutputStream multiOut;
    private MultiOutputStream multiErr;
    private JButton startStop;
    private Logger log = LoggerFactory.getLogger(getClass());
    private JFrame frame;
    private String name;
    private final Properties settings;
    private final JettyManager jettyManager;
    private final PluginManager<JettyConsolePlugin> pluginManager;

    public JettyConsole(Properties settings, JettyManager jettyManager, PluginManager<JettyConsolePlugin> pluginManager) {
        this.settings = settings;
        this.jettyManager = jettyManager;
        this.pluginManager = pluginManager;
    }

    public void init(final Configuration configuration) throws Exception {


        name = settings.getProperty("name");
        name = name == null ? "Jetty console" : name;
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", name);
        System.setProperty("derby.stream.error.field", "java.lang.System.out");
        frame = new JFrame(name);

        InputStream imageStream;

        imageStream = getClass().getClassLoader().getResourceAsStream("META-INF/jettyconsole/background-image.jpg");
        
        final BufferedImage image = ImageIO.read(imageStream);

        JPanel back = new JPanel() {
            protected void paintComponent(Graphics graphics) {
                graphics.drawImage(image, 0, 0, null);
            }

            public Dimension getPreferredSize() {
                return new Dimension(image.getWidth(), image.getHeight());
            }

        };
        back.setOpaque(false);
        back.setLayout(new BorderLayout());


        JPanel controls = new JPanel() {

            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics;

                Composite comp = g2.getComposite();
                Color c = g2.getColor();


                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4F));

                g2.setColor(Color.WHITE);

                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setComposite(comp);
                g2.setColor(c);
            }
        };
        controls.setOpaque(false);

        final JTextArea text = new JTextArea(10, 7) {

            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics;

                Composite comp = g2.getComposite();
                Color c = g2.getColor();


                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3F));

                g2.setColor(Color.BLACK);

                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setComposite(comp);
                g2.setColor(c);
                super.paintComponent(graphics);
            }
        };

        OutputStream os = new JTextAreaOutputStream(text);
        multiErr.addOutputStream(os);
        multiOut.addOutputStream(os);

        text.setMargin(new Insets(3, 3, 3, 3));
        text.setEditable(false);
        text.setOpaque(false);
        text.setWrapStyleWord(true);
        text.setForeground(Color.WHITE);
        text.setFont(text.getFont().deriveFont(11f));


        scroll = new JScrollPane(text);
        scroll.setOpaque(false);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

        Box b = Box.createVerticalBox();
        b.add(scroll);
        scroll.getViewport().setOpaque(false);
        Component corner = scroll.getCorner(JScrollPane.LOWER_RIGHT_CORNER);
        if (corner != null) {

        }
        b.add(controls);

        back.add(b, BorderLayout.SOUTH);
        final JTextField portField = new JTextField();
        portField.setText("8080");
        portField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                if (startAction.isEnabled()) {
                    startAction.actionPerformed(actionEvent);
                }
            }
        });



        startAction = new AbstractAction("Start") {

            public void actionPerformed(ActionEvent actionEvent) {
                final int port = Integer.parseInt(portField.getText());

                try (ServerSocket socket = new ServerSocket(port)){
                    // success, port is not in use
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Port " + port +" is already in use. Please select another port.");
                    return;
                }

                for(JettyConsolePlugin plugin : pluginManager.getPlugins()) {
                    for(StartOption option : plugin.getStartOptions()) {
                        if("port".equals(option.getName())) {
                            final String error = option.validate(portField.getText());
                            if(error != null) {
                                JOptionPane.showMessageDialog(null, error);
                                return;
                            }
                        }
                    }
                }
                
                frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                text.setText("");
                scroll.setVisible(true);
                scroll.invalidate();
                scroll.doLayout();
                scroll.paintImmediately(new Rectangle(scroll.getSize()));



                startAction.setEnabled(false);
                portField.setEnabled(false);

                new Thread() {
                    public void run() {
                        try {



                            jettyManager.startServer(configuration);

                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    stopAction.setEnabled(true);
                                    startStop.setAction(stopAction);
                                }
                            });

                            openUrl("http://localhost:" + port + configuration.getContextPath());
                        } catch (Exception e) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    startAction.setEnabled(true);
                                    startAction.setEnabled(false);
                                }
                            });

                            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                                }
                            });
                        }
                    }
                }.start();


            }

        };


        startStop = new JButton(startAction) {
        };
        startStop.setOpaque(false);

        stopAction = new AbstractAction("Stop") {

            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    stopAction.setEnabled(false);
                    jettyManager.stopServer();
                    portField.setEnabled(true);
                    startAction.setEnabled(true);
                    startStop.setAction(startAction);
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            }
        };

        jettyManager.addListener(new JettyManager.JettyListener() {
            public void serverStopped() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        stopAction.setEnabled(false);
                        portField.setEnabled(true);
                        startAction.setEnabled(true);
                        startStop.setAction(startAction);
                    }});
            }
        });
        JButton exit = new JButton(new AbstractAction("Exit") {

            public void actionPerformed(ActionEvent actionEvent) {
                conditionalExit();

            }
        });

        exit.setOpaque(false);
        stopAction.setEnabled(false);


        controls.add(new JLabel("Port: "));
        controls.add(portField);
        controls.add(startStop);
        controls.add(exit);

        frame.getContentPane().add(back);
        frame.pack();
        text.setMaximumSize(new Dimension(text.getWidth(), 70));
        scroll.setMaximumSize(new Dimension(text.getWidth(), 70));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((int) screenSize.getWidth() / 2 - frame.getWidth() / 2, (int) screenSize.getHeight() / 2 - frame.getHeight() / 2);
        frame.setVisible(true);
        scroll.setVisible(false);

        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent windowEvent) {
                conditionalExit();
            }

        });

        portField.setFocusable(true);
        portField.requestFocus(true);
        portField.setCaretPosition(portField.getText().length());

    }

    private void exit() {
        if (jettyManager != null) {
            try {
                jettyManager.stopServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        frame.dispose();
        System.exit(0);
    }

    private  void openUrl(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
            }
            else if (osName.startsWith("Windows"))
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else { //assume Unix or Linux
                String[] browsers = {
                        "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    if (Runtime.getRuntime().exec(
                            new String[] {"which", browsers[count]}).waitFor() == 0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    Runtime.getRuntime().exec(new String[] {browser, url});
            }
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }

    public void setMultiOut(MultiOutputStream multiOut) {
        this.multiOut = multiOut;
    }

    public void setMultiErr(MultiOutputStream multiErr) {
        this.multiErr = multiErr;
    }

    public void conditionalExit() {
        if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit from " +name +"?", "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            exit();
        }
    }
}
