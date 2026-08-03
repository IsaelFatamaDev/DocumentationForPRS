import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class DevInstaller extends JFrame {

    static final Color FONDO = new Color(0x121318);
    static final Color LATERAL = new Color(0x17181F);
    static final Color PANEL = new Color(0x1D1F28);
    static final Color PANEL_HOVER = new Color(0x252836);
    static final Color PANEL_SEL = new Color(0x232A45);
    static final Color BORDE = new Color(0x2A2D3A);
    static final Color TEXTO = new Color(0xE7E8EC);
    static final Color TEXTO_SUAVE = new Color(0x8B90A0);
    static final Color ACENTO = new Color(0x7C8CFF);
    static final Color ACENTO_HOVER = new Color(0x95A2FF);
    static final Color VERDE = new Color(0x59D499);
    static final Color ROJO = new Color(0xF07178);
    static final Color LOG_FONDO = new Color(0x0E0F13);

    static final String FUENTE = loadFont();

    static String loadFont() {
        try (java.io.InputStream in = DevInstaller.class.getResourceAsStream("/fonts/Inter.ttf")) {
            Font f = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f.getFamily();
        } catch (Exception e) {
            return "Segoe UI";
        }
    }

    static Font font(int estilo, int tam) {
        return new Font(FUENTE, estilo, tam);
    }

    static class IconoCat implements Icon {
        final int tipo;
        final Color color;

        IconoCat(int tipo, Color color) {
            this.tipo = tipo;
            this.color = color;
        }

        public int getIconWidth() {
            return 20;
        }

        public int getIconHeight() {
            return 20;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case 0 -> {
                    g2.fillRoundRect(3, 3, 6, 6, 3, 3);
                    g2.fillRoundRect(11, 3, 6, 6, 3, 3);
                    g2.fillRoundRect(3, 11, 6, 6, 3, 3);
                    g2.fillRoundRect(11, 11, 6, 6, 3, 3);
                }
                case 1 -> {
                    g2.drawPolyline(new int[] {7, 3, 7}, new int[] {5, 10, 15}, 3);
                    g2.drawPolyline(new int[] {13, 17, 13}, new int[] {5, 10, 15}, 3);
                }
                case 2 ->
                        g2.fillPolygon(
                                new Polygon(
                                        new int[] {11, 5, 9, 8, 15, 10, 12},
                                        new int[] {2, 11, 11, 18, 8, 8, 2},
                                        7));
                case 3 -> {
                    g2.drawOval(4, 3, 12, 5);
                    g2.drawLine(4, 6, 4, 15);
                    g2.drawLine(16, 6, 16, 15);
                    g2.drawArc(4, 12, 12, 5, 180, 180);
                    g2.drawArc(4, 7, 12, 5, 180, 180);
                }
                case 4 -> {
                    g2.drawRect(3, 3, 6, 6);
                    g2.drawRect(11, 3, 6, 6);
                    g2.drawRect(3, 11, 6, 6);
                    g2.drawRect(11, 11, 6, 6);
                }
                case 5 -> {
                    g2.drawLine(4, 6, 16, 6);
                    g2.fillOval(11, 4, 5, 5);
                    g2.drawLine(4, 13, 16, 13);
                    g2.fillOval(5, 11, 5, 5);
                }
                default -> {
                    g2.drawLine(5, 15, 5, 5);
                    g2.drawLine(5, 5, 15, 5);
                    g2.drawLine(10, 10, 15, 15);
                }
            }
            g2.dispose();
        }
    }

    static class Spinner extends JComponent {
        private double angulo = 0;
        private final Timer timer =
                new Timer(
                        16,
                        e -> {
                            angulo += 0.18;
                            repaint();
                        });

        Spinner() {
            setPreferredSize(new Dimension(18, 18));
            setVisible(false);
        }

        void start() {
            setVisible(true);
            timer.start();
        }

        void stop() {
            timer.stop();
            setVisible(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(ACENTO);
            g2.rotate(angulo, getWidth() / 2.0, getHeight() / 2.0);
            g2.drawArc(3, 3, getWidth() - 6, getHeight() - 6, 0, 280);
            g2.dispose();
        }
    }

    static class Boton extends JButton {
        final Color base;
        final Color hover;
        boolean encima = false;

        Boton(String txt, Color base, Color hover, Color texto, boolean negrita) {
            super(txt);
            this.base = base;
            this.hover = hover;
            setFont(font(negrita ? Font.BOLD : Font.PLAIN, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(texto);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            encima = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            encima = false;
                            repaint();
                        }
                    });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? (encima ? hover : base) : new Color(0x272935));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class App {
        final String nombre;
        final String wingetId;
        final String urlRespaldo;
        final int categoria;
        final TarjetaApp tarjeta;

        App(String nombre, String wingetId, int categoria, boolean marcada) {
            this(nombre, wingetId, categoria, marcada, null);
        }

        App(String nombre, String wingetId, int categoria, boolean marcada, String urlRespaldo) {
            this.nombre = nombre;
            this.wingetId = wingetId;
            this.categoria = categoria;
            this.urlRespaldo = urlRespaldo;
            this.tarjeta = new TarjetaApp(this, marcada);
            apps.add(this);
        }
    }

    class TarjetaApp extends JToggleButton {
        final App app;
        boolean encima = false;
        boolean ignorarToggle = false;

        TarjetaApp(App app, boolean marcada) {
            this.app = app;
            setSelected(marcada);
            setPreferredSize(new Dimension(230, 62));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            encima = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            encima = false;
                            repaint();
                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            if (app.wingetId.equals("NG") && inChip(e.getX())) {
                                ignorarToggle = true;
                                showVersions();
                            }
                        }
                    });
            addActionListener(
                    e -> {
                        if (ignorarToggle) {
                            setSelected(!isSelected());
                            ignorarToggle = false;
                        }
                        refreshCounters();
                    });
        }

        boolean inChip(int x) {
            return x >= getWidth() - 96 && x <= getWidth() - 52;
        }

        void showVersions() {
            JPopupMenu menu = new JPopupMenu();
            menu.setBackground(PANEL);
            menu.setBorder(BorderFactory.createLineBorder(BORDE, 1));
            for (String v : new String[] {"latest", "19", "18", "17", "16", "15"}) {
                JMenuItem item = new JMenuItem("Angular " + v);
                item.setFont(font(Font.PLAIN, 12));
                item.setOpaque(true);
                item.setBackground(PANEL);
                item.setForeground(v.equals(versionAngular) ? ACENTO_HOVER : TEXTO);
                item.addActionListener(
                        ev -> {
                            versionAngular = v;
                            repaint();
                        });
                menu.add(item);
            }
            menu.show(this, getWidth() - 100, getHeight() - 4);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(isSelected() ? PANEL_SEL : (encima ? PANEL_HOVER : PANEL));
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(isSelected() ? ACENTO : BORDE);
            g2.setStroke(new BasicStroke(isSelected() ? 1.6f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

            boolean esNg = app.wingetId.equals("NG");
            int margen = esNg ? 110 : 58;
            g2.setFont(font(Font.BOLD, 13));
            g2.setColor(isSelected() || encima ? Color.WHITE : TEXTO);
            g2.drawString(truncate(g2, app.nombre, w - margen), 14, 26);
            g2.setFont(font(Font.PLAIN, 11));
            g2.setColor(TEXTO_SUAVE);
            g2.drawString(truncate(g2, subtitle(), w - margen), 14, 44);

            if (esNg) {
                int chx = w - 96;
                int chy = h / 2 - 11;
                g2.setColor(new Color(0x2B3052));
                g2.fillRoundRect(chx, chy, 44, 22, 11, 11);
                g2.setColor(ACENTO_HOVER);
                g2.setFont(font(Font.BOLD, 11));
                String vtxt = versionAngular.equals("latest") ? "vlast" : "v" + versionAngular;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(vtxt, chx + (44 - fm.stringWidth(vtxt)) / 2, chy + 15);
            }

            int cx = w - 32;
            int cy = h / 2 - 9;
            if (isSelected()) {
                g2.setColor(ACENTO);
                g2.fillOval(cx, cy, 18, 18);
                g2.setColor(new Color(0x14151A));
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(
                        new int[] {cx + 5, cx + 8, cx + 13},
                        new int[] {cy + 9, cy + 12, cy + 6},
                        3);
            } else {
                g2.setColor(BORDE);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawOval(cx, cy, 18, 18);
            }
            g2.dispose();
        }

        String subtitle() {
            if (app.wingetId.equals("WSL")) return "wsl --install";
            if (app.wingetId.equals("NG")) return "npm - clic en el chip para elegir";
            if (app.wingetId.equals("FONT_INTER")) return "desde esta app";
            if (app.wingetId.equals("ENV_PATH")) return "variables de entorno";
            if (app.wingetId.equals("OHMYPOSH")) return "instala + tema + fuente + WT";
            return app.wingetId;
        }

        String truncate(Graphics2D g2, String txt, int ancho) {
            FontMetrics fm = g2.getFontMetrics();
            if (fm.stringWidth(txt) <= ancho) return txt;
            while (txt.length() > 3 && fm.stringWidth(txt + "...") > ancho) {
                txt = txt.substring(0, txt.length() - 1);
            }
            return txt + "...";
        }
    }

    class ItemLateral extends JToggleButton {
        final int indice;
        boolean encima = false;

        ItemLateral(int indice) {
            this.indice = indice;
            setPreferredSize(new Dimension(210, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            encima = true;
                            repaint();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            encima = false;
                            repaint();
                        }
                    });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (isSelected()) {
                g2.setColor(PANEL_SEL);
                g2.fillRoundRect(8, 3, w - 16, h - 6, 10, 10);
                g2.setColor(ACENTO);
                g2.fillRoundRect(8, 11, 3, h - 22, 2, 2);
            } else if (encima) {
                g2.setColor(PANEL);
                g2.fillRoundRect(8, 3, w - 16, h - 6, 10, 10);
            }
            new IconoCat(indice, isSelected() ? ACENTO : TEXTO_SUAVE)
                    .paintIcon(this, g2, 20, h / 2 - 10);
            g2.setFont(font(isSelected() ? Font.BOLD : Font.PLAIN, 13));
            g2.setColor(isSelected() ? Color.WHITE : TEXTO);
            g2.drawString(CATEGORIAS[indice], 48, h / 2 + 5);

            int sel = 0;
            int tot = 0;
            for (App a : apps) {
                if (indice == 0 || a.categoria == indice - 1) {
                    tot++;
                    if (a.tarjeta.isSelected()) sel++;
                }
            }
            String badge = sel + "/" + tot;
            g2.setFont(font(Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(badge) + 14;
            g2.setColor(sel > 0 ? new Color(0x2B3052) : new Color(0x20222C));
            g2.fillRoundRect(w - bw - 16, h / 2 - 9, bw, 18, 9, 9);
            g2.setColor(sel > 0 ? ACENTO_HOVER : TEXTO_SUAVE);
            g2.drawString(badge, w - bw - 16 + 7, h / 2 + 4);
            g2.dispose();
        }
    }

    static final String[] CATEGORIAS = {
        "Todas las apps",
        "Desarrollo",
        "Runtimes y lenguajes",
        "Bases de datos",
        "Comp. Microsoft",
        "Utilidades",
        "Fuentes de codigo"
    };

    private final List<App> apps = new ArrayList<>();
    private final List<ItemLateral> itemsLaterales = new ArrayList<>();
    private final JPanel rejilla = new JPanel();
    private final JTextField buscador = new JTextField();
    private final JTextArea log = new JTextArea();
    private String versionAngular = "19";
    private final Boton btnInstalar =
            new Boton("Instalar seleccionadas", ACENTO, ACENTO_HOVER, new Color(0x14151A), true);
    private final Boton btnTodo =
            new Boton("Marcar todo", new Color(0x262936), PANEL_HOVER, TEXTO, false);
    private final Boton btnNada =
            new Boton("Limpiar", new Color(0x262936), PANEL_HOVER, TEXTO, false);
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel estado = new JLabel("");
    private final Spinner spinner = new Spinner();
    private int categoriaActual = 0;
    private String rutaWinget = "winget";

    public DevInstaller() {
        super("Dev Installer - by IsaelFatamaDev");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 820);
        setMinimumSize(new Dimension(1080, 660));
        setLocationRelativeTo(null);

        createCatalog();

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        setContentPane(raiz);
        raiz.add(createSidebar(), BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        centro.add(createHeader(), BorderLayout.NORTH);

        rejilla.setOpaque(false);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(rejilla, BorderLayout.NORTH);
        JScrollPane scroll =
                new JScrollPane(
                        envoltura,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        styleScroll(scroll);
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(createBottomArea(), BorderLayout.SOUTH);
        raiz.add(centro, BorderLayout.CENTER);

        selectCategory(0);
        logLine("$ Dev Installer v2.0 - by IsaelFatamaDev");
        logLine("$ Marca tus apps y pulsa 'Instalar seleccionadas'. Todo en su ultima version.");
        refreshCounters();
    }

    private void createCatalog() {
        new App("Git", "Git.Git", 0, true);
        new App("GitHub CLI (gh)", "GitHub.cli", 0, true);
        new App(
                "Visual Studio Code",
                "Microsoft.VisualStudioCode",
                0,
                true,
                "https://code.visualstudio.com/sha/download?build=stable&os=win32-x64-user");
        new App(
                "IntelliJ IDEA Community",
                "JetBrains.IntelliJIDEA.Community",
                0,
                true,
                "https://download.jetbrains.com/product?code=IIC&latest&distribution=windows");
        new App(
                "IntelliJ IDEA Ultimate",
                "JetBrains.IntelliJIDEA.Ultimate",
                0,
                false,
                "https://download.jetbrains.com/product?code=IIU&latest&distribution=windows");
        new App("Apache Maven", "Apache.Maven", 0, true);
        new App("Obsidian", "Obsidian.Obsidian", 0, true);
        new App(
                "Postman",
                "Postman.Postman",
                0,
                false,
                "https://dl.pstmn.io/download/latest/win64");
        new App("Bruno", "Bruno.Bruno", 0, false);
        new App(
                "Docker Desktop",
                "Docker.DockerDesktop",
                0,
                false,
                "https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe");
        new App(
                "Eclipse IDE (Java)",
                "EclipseFoundation.EclipseIDEforJavaDevelopers",
                0,
                false,
                "https://download.eclipse.org/oomph/products/eclipse-inst-jre-win64.exe");
        new App("Apache NetBeans", "Apache.NetBeans", 0, false);

        new App("Java 8 (Temurin JDK)", "EclipseAdoptium.Temurin.8.JDK", 1, false);
        new App("Java 11 (Temurin JDK)", "EclipseAdoptium.Temurin.11.JDK", 1, false);
        new App("Java 17 (Temurin JDK)", "EclipseAdoptium.Temurin.17.JDK", 1, true);
        new App("Java 21 (Temurin JDK)", "EclipseAdoptium.Temurin.21.JDK", 1, true);
        new App("Java 25 (Temurin JDK)", "EclipseAdoptium.Temurin.25.JDK", 1, false);
        new App("Node.js LTS", "OpenJS.NodeJS.LTS", 1, true);
        new App("Node.js Current", "OpenJS.NodeJS", 1, false);
        new App(
                "NVM for Windows",
                "CoreyButler.NVMforWindows",
                1,
                false,
                "https://github.com/coreybutler/nvm-windows/releases/latest/download/nvm-setup.exe");
        new App("pnpm", "pnpm.pnpm", 1, true);
        new App("Angular CLI", "NG", 1, true);
        new App("Python 3.10", "Python.Python.3.10", 1, false);
        new App("Python 3.11", "Python.Python.3.11", 1, false);
        new App("Python 3.12", "Python.Python.3.12", 1, false);
        new App("Python 3.13", "Python.Python.3.13", 1, false);
        new App("Python 3.14", "Python.Python.3.14", 1, false);
        new App("Go", "GoLang.Go", 1, false);
        new App(
                "Rust (rustup)",
                "Rustlang.Rustup",
                1,
                false,
                "https://static.rust-lang.org/rustup/dist/x86_64-pc-windows-msvc/rustup-init.exe");
        new App(
                ".NET SDK 8",
                "Microsoft.DotNet.SDK.8",
                1,
                false,
                "https://aka.ms/dotnet/8.0/dotnet-sdk-win-x64.exe");
        new App("Configurar PATH y JAVA_HOME", "ENV_PATH", 1, true);

        new App(
                "SSMS (SQL Server)",
                "Microsoft.SQLServerManagementStudio",
                2,
                true,
                "https://aka.ms/ssmsfullsetup");
        new App("pgAdmin (PostgreSQL)", "PostgreSQL.pgAdmin", 2, true);
        new App("MySQL Workbench", "Oracle.MySQLWorkbench", 2, false);
        new App("HeidiSQL", "HeidiSQL.HeidiSQL", 2, false);
        new App(
                "DBeaver",
                "dbeaver.dbeaver",
                2,
                true,
                "https://dbeaver.io/files/dbeaver-ce-latest-x86_64-setup.exe");
        new App(
                "DataGrip (JetBrains)",
                "JetBrains.DataGrip",
                2,
                false,
                "https://download.jetbrains.com/product?code=DG&latest&distribution=windows");
        new App("MongoDB Compass", "MongoDB.Compass.Full", 2, false);
        new App("Redis Desktop Manager", "qishibo.AnotherRedisDesktopManager", 2, false);
        new App("Azure Data Studio", "Microsoft.AzureDataStudio", 2, false);

        new App(
                "VC++ Redist 2015+ x64",
                "Microsoft.VCRedist.2015+.x64",
                3,
                true,
                "https://aka.ms/vs/17/release/vc_redist.x64.exe");
        new App(
                "VC++ Redist 2015+ x86",
                "Microsoft.VCRedist.2015+.x86",
                3,
                true,
                "https://aka.ms/vs/17/release/vc_redist.x86.exe");
        new App(
                "VC++ Redist 2013 x64",
                "Microsoft.VCRedist.2013.x64",
                3,
                false,
                "https://aka.ms/highdpimfc2013x64enu");
        new App(
                "VC++ Redist 2012 x64",
                "Microsoft.VCRedist.2012.x64",
                3,
                false,
                "https://download.microsoft.com/download/1/6/B/16B06F60-3B20-4FF2-B699-5E9B7962F9AE/VSU_4/vcredist_x64.exe");
        new App(
                ".NET Desktop Runtime 8",
                "Microsoft.DotNet.DesktopRuntime.8",
                3,
                true,
                "https://aka.ms/dotnet/8.0/windowsdesktop-runtime-win-x64.exe");
        new App(
                ".NET Desktop Runtime 9",
                "Microsoft.DotNet.DesktopRuntime.9",
                3,
                false,
                "https://aka.ms/dotnet/9.0/windowsdesktop-runtime-win-x64.exe");
        new App(
                ".NET Runtime 8",
                "Microsoft.DotNet.Runtime.8",
                3,
                false,
                "https://aka.ms/dotnet/8.0/dotnet-runtime-win-x64.exe");
        new App(
                ".NET Framework 4.8.1 Dev",
                "Microsoft.DotNet.Framework.DeveloperPack_4",
                3,
                false,
                "https://go.microsoft.com/fwlink/?linkid=2203306");
        new App(
                "DirectX Runtime",
                "Microsoft.DirectX",
                3,
                false,
                "https://download.microsoft.com/download/1/7/1/1718CCC4-6315-4D8E-9543-8E28A4E18C4C/dxwebsetup.exe");
        new App(
                "Edge WebView2",
                "Microsoft.EdgeWebView2Runtime",
                3,
                false,
                "https://go.microsoft.com/fwlink/p/?LinkId=2124703");

        new App("Windows Terminal", "Microsoft.WindowsTerminal", 4, true);
        new App("Oh My Posh (terminal pro)", "OHMYPOSH", 4, true);
        new App("PowerShell 7", "Microsoft.PowerShell", 4, false);
        new App("Notepad++", "Notepad++.Notepad++", 4, false);
        new App("7-Zip", "7zip.7zip", 4, true);
        new App("WinRAR", "RARLab.WinRAR", 4, true);
        new App(
                "Google Chrome",
                "Google.Chrome",
                4,
                false,
                "https://dl.google.com/chrome/install/latest/chrome_installer.exe");
        new App("Brave", "Brave.Brave", 4, false, "https://laptop-updates.brave.com/latest/winx64");
        new App("FileZilla (FTP)", "TimKosse.FileZilla.Client", 4, false);
        new App(
                "WinSCP (SFTP)",
                "WinSCP.WinSCP",
                4,
                false,
                "https://sourceforge.net/projects/winscp/files/latest/download");
        new App("PuTTY (SSH)", "PuTTY.PuTTY", 4, false);
        new App("Figma", "Figma.Figma", 4, false, "https://desktop.figma.com/win/FigmaSetup.exe");
        new App(
                "Notion",
                "Notion.Notion",
                4,
                false,
                "https://www.notion.so/desktop/windows/download");
        new App("WSL (Subsistema Linux)", "WSL", 4, false);

        new App("Cascadia Code", "Microsoft.CascadiaCode", 5, true);
        new App("JetBrains Mono NF", "DEVCOM.JetBrainsMonoNerdFont", 5, true);
        new App("Fira Code NF", "DEVCOM.FiraCodeNerdFont", 5, false);
        new App("Hack NF", "DEVCOM.HackNerdFont", 5, false);
        new App("Meslo NF", "DEVCOM.MesloLGNerdFont", 5, false);
        new App("Inter (al sistema)", "FONT_INTER", 5, false);
    }

    private JComponent createSidebar() {
        JPanel lateral = new JPanel();
        lateral.setBackground(LATERAL);
        lateral.setPreferredSize(new Dimension(238, 100));
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDE));

        JLabel marca = new JLabel("Dev Installer");
        marca.setFont(font(Font.BOLD, 19));
        marca.setForeground(Color.WHITE);
        marca.setBorder(BorderFactory.createEmptyBorder(22, 22, 2, 0));
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(marca);

        JLabel version = new JLabel("v2.0  -  winget edition");
        version.setFont(font(Font.PLAIN, 11));
        version.setForeground(TEXTO_SUAVE);
        version.setBorder(BorderFactory.createEmptyBorder(0, 23, 18, 0));
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(version);

        ButtonGroup grupoNav = new ButtonGroup();
        for (int i = 0; i < CATEGORIAS.length; i++) {
            ItemLateral item = new ItemLateral(i);
            int idx = i;
            item.addActionListener(e -> selectCategory(idx));
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            grupoNav.add(item);
            itemsLaterales.add(item);
            lateral.add(item);
        }
        itemsLaterales.get(0).setSelected(true);

        lateral.add(Box.createVerticalGlue());
        JLabel creditos = new JLabel("<html>(c) 2026 IsaelFatamaDev<br>Java Swing + winget</html>");
        creditos.setFont(font(Font.PLAIN, 11));
        creditos.setForeground(TEXTO_SUAVE);
        creditos.setBorder(BorderFactory.createEmptyBorder(0, 23, 18, 0));
        creditos.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(creditos);
        return lateral;
    }

    private JComponent createHeader() {
        JPanel cabecera = new JPanel(new BorderLayout(14, 0));
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createEmptyBorder(0, 2, 16, 0));

        buscador.setFont(font(Font.PLAIN, 13));
        buscador.setForeground(TEXTO);
        buscador.setCaretColor(TEXTO);
        buscador.setOpaque(false);
        buscador.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        buscador.getDocument()
                .addDocumentListener(
                        new javax.swing.event.DocumentListener() {
                            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                                rebuildGrid();
                            }

                            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                                rebuildGrid();
                            }

                            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                                rebuildGrid();
                            }
                        });
        JPanel cajaBuscador =
                new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(PANEL);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                        g2.setColor(BORDE);
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                        if (buscador.getText().isEmpty() && !buscador.hasFocus()) {
                            g2.setRenderingHint(
                                    RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                            g2.setFont(font(Font.PLAIN, 13));
                            g2.setColor(TEXTO_SUAVE);
                            g2.drawString("Buscar aplicacion...", 16, getHeight() / 2 + 5);
                        }
                        g2.dispose();
                    }
                };
        cajaBuscador.setOpaque(false);
        cajaBuscador.add(buscador, BorderLayout.CENTER);
        cajaBuscador.setPreferredSize(new Dimension(300, 38));
        cabecera.add(cajaBuscador, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(btnTodo);
        derecha.add(btnNada);
        cabecera.add(derecha, BorderLayout.EAST);

        btnTodo.addActionListener(e -> markAll(true));
        btnNada.addActionListener(e -> markAll(false));
        return cabecera;
    }

    private JComponent createBottomArea() {
        JPanel abajo = new JPanel(new BorderLayout(0, 10));
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel accion = new JPanel(new BorderLayout());
        accion.setOpaque(false);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(btnInstalar);
        izq.add(spinner);
        estado.setFont(font(Font.PLAIN, 12));
        estado.setForeground(TEXTO_SUAVE);
        izq.add(estado);
        accion.add(izq, BorderLayout.WEST);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        der.setOpaque(false);
        progreso.setPreferredSize(new Dimension(240, 8));
        progreso.setBorderPainted(false);
        progreso.setBackground(new Color(0x262936));
        progreso.setForeground(ACENTO);
        JPanel envolProg = new JPanel(new GridBagLayout());
        envolProg.setOpaque(false);
        envolProg.add(progreso);
        der.add(envolProg);
        accion.add(der, BorderLayout.EAST);
        abajo.add(accion, BorderLayout.NORTH);

        log.setEditable(false);
        Font mono = new Font("Cascadia Code", Font.PLAIN, 12);
        if (!mono.getFamily().equals("Cascadia Code")) {
            mono = new Font("Consolas", Font.PLAIN, 12);
            if (!mono.getFamily().equals("Consolas")) {
                mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
            }
        }
        log.setFont(mono);
        log.setBackground(LOG_FONDO);
        log.setForeground(new Color(0xAEB4C4));
        log.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JScrollPane scrollLog = new JScrollPane(log);
        scrollLog.setPreferredSize(new Dimension(100, 150));
        scrollLog.setBorder(BorderFactory.createLineBorder(BORDE, 1, true));
        scrollLog.getViewport().setBackground(LOG_FONDO);
        styleScroll(scrollLog);
        abajo.add(scrollLog, BorderLayout.CENTER);
        btnInstalar.addActionListener(e -> install());
        return abajo;
    }

    private void styleScroll(JScrollPane s) {
        s.getVerticalScrollBar().setUnitIncrement(14);
        s.getVerticalScrollBar()
                .setUI(
                        new javax.swing.plaf.basic.BasicScrollBarUI() {
                            @Override
                            protected void configureScrollBarColors() {
                                thumbColor = new Color(0x353948);
                            }

                            @Override
                            protected JButton createDecreaseButton(int o) {
                                return zeroButton();
                            }

                            @Override
                            protected JButton createIncreaseButton(int o) {
                                return zeroButton();
                            }

                            private JButton zeroButton() {
                                JButton b = new JButton();
                                b.setPreferredSize(new Dimension(0, 0));
                                return b;
                            }

                            @Override
                            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                                Graphics2D g2 = (Graphics2D) g.create();
                                g2.setRenderingHint(
                                        RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(thumbColor);
                                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 8, 8);
                                g2.dispose();
                            }

                            @Override
                            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                                g.setColor(FONDO);
                                g.fillRect(r.x, r.y, r.width, r.height);
                            }
                        });
        s.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }

    private void selectCategory(int indice) {
        categoriaActual = indice;
        itemsLaterales.get(indice).setSelected(true);
        rebuildGrid();
    }

    private boolean matchesFilter(App a) {
        String filtro = buscador.getText().trim().toLowerCase();
        boolean deCategoria = categoriaActual == 0 || a.categoria == categoriaActual - 1;
        boolean coincide =
                filtro.isEmpty()
                        || a.nombre.toLowerCase().contains(filtro)
                        || a.wingetId.toLowerCase().contains(filtro);
        return deCategoria && coincide;
    }

    private void rebuildGrid() {
        rejilla.removeAll();
        rejilla.setLayout(new GridLayout(0, 3, 12, 12));
        for (App a : apps) if (matchesFilter(a)) rejilla.add(a.tarjeta);
        rejilla.revalidate();
        rejilla.repaint();
    }

    private void markAll(boolean valor) {
        for (App a : apps) if (matchesFilter(a)) a.tarjeta.setSelected(valor);
        refreshCounters();
    }

    private void refreshCounters() {
        long n = apps.stream().filter(a -> a.tarjeta.isSelected()).count();
        estado.setText(n + " seleccionadas");
        estado.setForeground(TEXTO_SUAVE);
        for (ItemLateral item : itemsLaterales) item.repaint();
    }

    private void logLine(String linea) {
        SwingUtilities.invokeLater(
                () -> {
                    log.append(linea + "\n");
                    log.setCaretPosition(log.getDocument().getLength());
                });
    }

    private void setStatus(String txt, Color color) {
        SwingUtilities.invokeLater(
                () -> {
                    estado.setText(txt);
                    estado.setForeground(color);
                });
    }

    private void install() {
        List<App> seleccionadas = new ArrayList<>();
        for (App a : apps) if (a.tarjeta.isSelected()) seleccionadas.add(a);
        if (seleccionadas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No marcaste ninguna aplicacion.");
            return;
        }
        btnInstalar.setEnabled(false);
        spinner.start();
        progreso.setMaximum(seleccionadas.size());
        progreso.setValue(0);

        new Thread(
                        () -> {
                            int ok = 0;
                            int fallo = 0;
                            int hechas = 0;
                            int total = seleccionadas.size();

                            if (!hasWinget() && !repairWinget()) {
                                logLine("[ERROR] No se pudo instalar winget automaticamente.");
                                logLine(
                                        "        Abre Microsoft Store, busca 'App Installer',"
                                            + " instalalo y reintenta.");
                                setStatus("winget no disponible", ROJO);
                                SwingUtilities.invokeLater(
                                        () -> {
                                            spinner.stop();
                                            btnInstalar.setEnabled(true);
                                        });
                                return;
                            }

                            for (App a : seleccionadas) {
                                logLine("");
                                logLine("==> Instalando " + a.nombre + "...");
                                setStatus(
                                        "Instalando "
                                                + a.nombre
                                                + "  ("
                                                + (hechas + 1)
                                                + "/"
                                                + total
                                                + ")",
                                        ACENTO);
                                int codigo;
                                if (a.wingetId.equals("WSL")) {
                                    logLine(
                                            "    Habilita caracteristicas de Windows e instala"
                                                + " Ubuntu.");
                                    logLine("    Requiere administrador y REINICIAR al terminar.");
                                    codigo = runCommand("cmd.exe", "/c", "wsl --install");
                                } else if (a.wingetId.equals("NG")) {
                                    String paquete =
                                            versionAngular.equals("latest")
                                                    ? "@angular/cli"
                                                    : "@angular/cli@" + versionAngular;
                                    logLine("    npm install -g " + paquete);
                                    codigo =
                                            runCommand(
                                                    "cmd.exe", "/c", "npm install -g " + paquete);
                                    if (codigo != 0) {
                                        logLine(
                                                "    Si acabas de instalar Node.js, abre una"
                                                    + " terminal NUEVA y ejecuta:");
                                        logLine("    npm install -g " + paquete);
                                    }
                                } else if (a.wingetId.equals("FONT_INTER")) {
                                    codigo = installInterFont();
                                } else if (a.wingetId.equals("ENV_PATH")) {
                                    codigo = configureEnvironment();
                                } else if (a.wingetId.equals("OHMYPOSH")) {
                                    codigo = configureOhMyPosh();
                                } else {
                                    codigo =
                                            runCommand(
                                                    rutaWinget,
                                                    "install",
                                                    "--id",
                                                    a.wingetId,
                                                    "-e",
                                                    "--silent",
                                                    "--accept-source-agreements",
                                                    "--accept-package-agreements");
                                    if (codigo != 0 && a.urlRespaldo != null) {
                                        logLine(
                                                "    winget fallo (codigo "
                                                        + codigo
                                                        + "). Probando descarga directa...");
                                        codigo = downloadAndInstall(a);
                                    }
                                }
                                if (codigo == 0) {
                                    ok++;
                                    logLine("    [OK] " + a.nombre + " instalado.");
                                } else {
                                    fallo++;
                                    logLine(
                                            "    [ERROR] "
                                                    + a.nombre
                                                    + " termino con codigo "
                                                    + codigo
                                                    + " (puede que ya este instalado).");
                                }
                                hechas++;
                                int v = hechas;
                                SwingUtilities.invokeLater(() -> progreso.setValue(v));
                            }

                            logLine("");
                            logLine("========================================");
                            logLine("Terminado: " + ok + " correctas, " + fallo + " con error.");
                            logLine(
                                    "Abre una terminal NUEVA para que se reconozcan los comandos"
                                        + " (git, node, mvn...).");
                            setStatus(
                                    "Terminado: " + ok + " OK, " + fallo + " con error",
                                    fallo == 0 ? VERDE : ROJO);
                            SwingUtilities.invokeLater(
                                    () -> {
                                        spinner.stop();
                                        btnInstalar.setEnabled(true);
                                    });
                        },
                        "instalador")
                .start();
    }

    private boolean hasWinget() {
        if (runSilent(rutaWinget, "--version") == 0) return true;
        String local = System.getenv("LOCALAPPDATA");
        if (local != null) {
            java.io.File f = new java.io.File(local + "\\Microsoft\\WindowsApps\\winget.exe");
            if (f.exists() && runSilent(f.getAbsolutePath(), "--version") == 0) {
                rutaWinget = f.getAbsolutePath();
                return true;
            }
        }
        return false;
    }

    private boolean repairWinget() {
        logLine("");
        logLine("==> winget no encontrado. Intentando repararlo...");
        setStatus("Instalando winget...", ACENTO);
        runCommand(
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                "Add-AppxPackage -RegisterByFamilyName -MainPackage"
                    + " Microsoft.DesktopAppInstaller_8wekyb3d8bbwe");
        if (hasWinget()) {
            logLine("    [OK] winget reparado.");
            return true;
        }
        logLine("==> Descargando App Installer (winget) desde Microsoft...");
        runCommand(
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                "$ProgressPreference='SilentlyContinue';"
                        + "$p=\"$env:TEMP\\winget.msixbundle\";"
                        + "Invoke-WebRequest -Uri https://aka.ms/getwinget -OutFile $p;"
                        + "Add-AppxPackage -Path $p");
        if (hasWinget()) {
            logLine("    [OK] winget instalado.");
            return true;
        }
        return false;
    }

    private int downloadAndInstall(App a) {
        try {
            logLine("    Descargando: " + a.urlRespaldo);
            java.net.http.HttpClient cliente =
                    java.net.http.HttpClient.newBuilder()
                            .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                            .connectTimeout(java.time.Duration.ofSeconds(30))
                            .build();
            java.net.http.HttpRequest peticion =
                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(a.urlRespaldo))
                            .header("User-Agent", "DevInstaller/2.0")
                            .build();
            String ext = a.urlRespaldo.toLowerCase().contains(".msi") ? ".msi" : ".exe";
            java.io.File destino = java.io.File.createTempFile("instalador-", ext);
            java.net.http.HttpResponse<java.nio.file.Path> resp =
                    cliente.send(
                            peticion,
                            java.net.http.HttpResponse.BodyHandlers.ofFile(destino.toPath()));
            if (resp.statusCode() >= 400 || destino.length() == 0) {
                logLine("    [ERROR] La descarga fallo (HTTP " + resp.statusCode() + ").");
                return -1;
            }
            logLine(
                    "    Descargado ("
                            + Math.max(1, destino.length() / (1024 * 1024))
                            + " MB). Ejecutando instalador, sigue las ventanas que aparezcan...");
            if (ext.equals(".msi")) return runCommand("msiexec", "/i", destino.getAbsolutePath());
            return runCommand(destino.getAbsolutePath());
        } catch (Exception e) {
            logLine("    [ERROR] " + e.getMessage());
            return -1;
        }
    }

    private int runScript(String contenido) {
        try {
            java.io.File script = java.io.File.createTempFile("devinstaller-", ".ps1");
            java.nio.file.Files.writeString(script.toPath(), contenido);
            return runCommand(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    script.getAbsolutePath());
        } catch (Exception e) {
            logLine("    [ERROR] " + e.getMessage());
            return -1;
        }
    }

    private int configureEnvironment() {
        logLine("    Configurando JAVA_HOME y PATH del usuario...");
        return runScript(
                String.join(
                        "\n",
                        "$ErrorActionPreference='SilentlyContinue'",
                        "$jdk = Get-ChildItem 'C:\\Program Files\\Eclipse Adoptium' -Directory |",
                        "  Where-Object Name -like 'jdk-*' | Sort-Object Name -Descending |"
                            + " Select-Object -First 1",
                        "if ($jdk) {",
                        "  [Environment]::SetEnvironmentVariable('JAVA_HOME', $jdk.FullName,"
                            + " 'User')",
                        "  Write-Output ('JAVA_HOME = ' + $jdk.FullName)",
                        "  $path = [Environment]::GetEnvironmentVariable('Path','User')",
                        "  $bin = Join-Path $jdk.FullName 'bin'",
                        "  if ($path -notlike ('*' + $bin + '*')) {",
                        "    [Environment]::SetEnvironmentVariable('Path', ($path.TrimEnd(';') +"
                            + " ';' + $bin), 'User')",
                        "    Write-Output 'PATH: agregado bin del JDK'",
                        "  } else { Write-Output 'PATH: el JDK ya estaba' }",
                        "} else { Write-Output 'No se encontro JDK de Adoptium (instala Java"
                            + " primero)' }",
                        "$mvn = Get-ChildItem 'C:\\Program"
                            + " Files\\Apache\\Maven','C:\\ProgramData\\chocolatey' -Directory"
                            + " 2>$null |",
                        "  Where-Object Name -like 'apache-maven*' | Select-Object -First 1",
                        "if ($mvn) {",
                        "  [Environment]::SetEnvironmentVariable('MAVEN_HOME', $mvn.FullName,"
                            + " 'User')",
                        "  Write-Output ('MAVEN_HOME = ' + $mvn.FullName)",
                        "}",
                        "Write-Output 'Variables configuradas. Abre una terminal NUEVA para"
                            + " verlas.'",
                        "exit 0"));
    }

    private int configureOhMyPosh() {
        logLine("    Paso 1/3: instalando Oh My Posh...");
        runCommand(
                rutaWinget,
                "install",
                "--id",
                "JanDeDobbeleer.OhMyPosh",
                "-e",
                "--silent",
                "--accept-source-agreements",
                "--accept-package-agreements");
        logLine("    Paso 2/3: instalando JetBrains Mono Nerd Font (para los iconos)...");
        runCommand(
                rutaWinget,
                "install",
                "--id",
                "DEVCOM.JetBrainsMonoNerdFont",
                "-e",
                "--silent",
                "--accept-source-agreements",
                "--accept-package-agreements");
        logLine("    Paso 3/3: configurando perfiles de PowerShell y Windows Terminal...");
        return runScript(
                String.join(
                        "\n",
                        "$ErrorActionPreference='SilentlyContinue'",
                        "$perfiles = @(",
                        "  (Join-Path ([Environment]::GetFolderPath('MyDocuments'))"
                            + " 'WindowsPowerShell\\Microsoft.PowerShell_profile.ps1'),",
                        "  (Join-Path ([Environment]::GetFolderPath('MyDocuments'))"
                            + " 'PowerShell\\Microsoft.PowerShell_profile.ps1')",
                        ")",
                        "$linea = 'oh-my-posh init pwsh --config"
                            + " \"$env:POSH_THEMES_PATH\\atomic.omp.json\" | Invoke-Expression'",
                        "foreach ($p in $perfiles) {",
                        "  New-Item -ItemType Directory -Force -Path (Split-Path $p) | Out-Null",
                        "  if (!(Test-Path $p) -or !(Select-String -Path $p -SimpleMatch"
                            + " 'oh-my-posh init' -Quiet)) {",
                        "    Add-Content -Path $p -Value $linea",
                        "    Write-Output ('Perfil configurado: ' + $p)",
                        "  } else { Write-Output ('Ya estaba configurado: ' + $p) }",
                        "}",
                        "$ws ="
                            + " \"$env:LOCALAPPDATA\\Packages\\Microsoft.WindowsTerminal_8wekyb3d8bbwe\\LocalState\\settings.json\"",
                        "if (Test-Path $ws) {",
                        "  $j = Get-Content $ws -Raw | ConvertFrom-Json",
                        "  if (-not $j.profiles.defaults) {",
                        "    $j.profiles | Add-Member -MemberType NoteProperty -Name defaults"
                            + " -Value ([pscustomobject]@{}) -Force",
                        "  }",
                        "  $fuenteWT = [pscustomobject]@{ face = 'JetBrainsMono Nerd Font'; size ="
                            + " 11 }",
                        "  $j.profiles.defaults | Add-Member -MemberType NoteProperty -Name font"
                            + " -Value $fuenteWT -Force",
                        "  $j.profiles.defaults | Add-Member -MemberType NoteProperty -Name"
                            + " colorScheme -Value 'One Half Dark' -Force",
                        "  $j.profiles.defaults | Add-Member -MemberType NoteProperty -Name opacity"
                            + " -Value 96 -Force",
                        "  $j.profiles.defaults | Add-Member -MemberType NoteProperty -Name"
                            + " useAcrylic -Value $true -Force",
                        "  $j | ConvertTo-Json -Depth 32 | Set-Content $ws -Encoding UTF8",
                        "  Write-Output 'Windows Terminal: fuente Nerd Font, esquema y opacidad"
                            + " configurados.'",
                        "} else { Write-Output 'Windows Terminal aun sin abrir: abrelo una vez y"
                            + " reinstala esta opcion.' }",
                        "reg add 'HKCU\\Console\\%%Startup' /v DelegationConsole /t REG_SZ /d"
                            + " '{2EACA947-7F5F-4CFA-BA87-8F7FBEEFBE69}' /f | Out-Null",
                        "reg add 'HKCU\\Console\\%%Startup' /v DelegationTerminal /t REG_SZ /d"
                            + " '{E12CFF52-A866-4C77-9A90-F570A7AA2C6B}' /f | Out-Null",
                        "Write-Output 'Windows Terminal establecida como terminal predeterminada.'",
                        "Write-Output 'Listo: abre Windows Terminal y disfruta tu prompt con"
                            + " estilo.'",
                        "exit 0"));
    }

    private int installInterFont() {
        try {
            java.io.File tmp = java.io.File.createTempFile("Inter", ".ttf");
            try (java.io.InputStream in =
                            DevInstaller.class.getResourceAsStream("/fonts/Inter.ttf");
                    java.io.OutputStream salida = new java.io.FileOutputStream(tmp)) {
                in.transferTo(salida);
            }
            String comando =
                    "$dir=\"$env:LOCALAPPDATA\\Microsoft\\Windows\\Fonts\";"
                            + "New-Item -ItemType Directory -Force -Path $dir | Out-Null;"
                            + "$destino=Join-Path $dir 'Inter.ttf';"
                            + "Copy-Item -Force '"
                            + tmp.getAbsolutePath()
                            + "' $destino;$reg='HKCU:\\Software\\Microsoft\\Windows"
                            + " NT\\CurrentVersion\\Fonts';New-Item -Path $reg -Force |"
                            + " Out-Null;New-ItemProperty -Path $reg -Name 'Inter (TrueType)'"
                            + " -Value $destino -PropertyType String -Force | Out-Null";
            return runCommand(
                    "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", comando);
        } catch (Exception e) {
            logLine("    [ERROR] " + e.getMessage());
            return -1;
        }
    }

    private int runSilent(String... comando) {
        try {
            Process p = new ProcessBuilder(comando).redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor();
        } catch (Exception e) {
            return -1;
        }
    }

    private int runCommand(String... comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String linea;
                while ((linea = r.readLine()) != null) {
                    String limpia = linea.trim();
                    if (!limpia.isEmpty() && !limpia.matches("^[\\\\/|\\-\\s█▒░]+.*%?$")) {
                        logLine("    " + limpia);
                    }
                }
            }
            return p.waitFor();
        } catch (Exception e) {
            logLine("    [ERROR] " + e.getMessage());
            return -1;
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new DevInstaller().setVisible(true));
    }
}
