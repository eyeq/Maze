package maze.config;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class Configuration {
    public static final String ALLOWED_TO_USE_NAME = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String NEW_LINE = System.lineSeparator();

    protected final File file;

    public String encoding = DEFAULT_ENCODING;
    private boolean isChanged = false;
    private Map<String, String> values = new LinkedHashMap<>();
    private Map<String, String> comments = new LinkedHashMap<>();

    public Configuration(File file) {
        this.file = file;
        try {
            load();
        } catch(Throwable e) {
            e.printStackTrace();
            File fileBak = new File(file.getAbsolutePath() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".errored");
            file.renameTo(fileBak);
            load();
        }
    }

    public void load() {
        BufferedReader reader = null;
        try {
            if(file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if(!file.exists()) {
                values.clear();
                comments.clear();
                if(!file.createNewFile()) {
                    return;
                }
            }
            if(!file.canRead()) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            String comment = null;
            for(int lineNum = 1; ;lineNum++)
            {
                String line = reader.readLine();
                if(line == null) {
                    break;
                }

                boolean skip = false;
                int nameStart = -1, nameEnd = -1;
                for(int i = 0; i < line.length() && !skip; ++i) {
                    final char ch = line.charAt(i);
                    if(Character.isLetterOrDigit(ch) || ALLOWED_TO_USE_NAME.indexOf(ch) != -1) {
                        if(nameStart == -1) {
                            nameStart = i;
                        }
                        nameEnd = i;
                    } else if(!Character.isWhitespace(ch)) {
                        switch(ch) {
                        case '#':
                            skip = true;
                            if(comment == null) {
                                comment = line;
                            } else {
                                comment += NEW_LINE + line;
                            }
                            continue;
                        case '=':
                            skip = true;
                            String key = line.substring(nameStart, nameEnd + 1);
                            set(key, line.substring(i + 1), comment);
                            comment = null;
                            break;
                        default:
                            throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", ch, file.getName(), lineNum));
                        }
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                }
            }
        }

        setChanged(false);
    }

    public void save() {
        try {
            if(file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            if(!file.exists() && !file.createNewFile()) {
                return;
            }
            if(!file.canWrite()) {
                return;
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            save(writer);
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        setChanged(false);
    }

    private void save(BufferedWriter writer) throws IOException {
        for(String key : values.keySet()) {
            if(comments.containsKey(key)) {
                String comment = comments.get(key);
                if(comment.indexOf(0) != '#') {
                    writer.write('#');
                }
                writer.write(comment);
                writer.newLine();
            }
            writer.write(key + " = " + values.get(key));
            writer.newLine();
        }
    }

    public boolean hasKey(String key) {
        return values.containsKey(key);
    }

    public boolean get(String key, boolean defaultValue) {
        return get(key, defaultValue, null);
    }

    public boolean get(String key, boolean defaultValue, String defaultComment) {
        String str = get(key, Boolean.toString(defaultValue), defaultComment);
        if(str == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(str);
    }

    public int get(String key, int defaultValue) {
        return get(key, defaultValue, null);
    }

    public int get(String key, int defaultValue, String defaultComment) {
        String str = get(key, Integer.toString(defaultValue), defaultComment);
        if(str == null) {
            return defaultValue;
        }
        return Integer.parseInt(str);
    }

    public double get(String key, double defaultValue) {
        return get(key, defaultValue, null);
    }

    public double get(String key, double defaultValue, String defaultComment) {
        String str = get(key, Double.toString(defaultValue), defaultComment);
        if(str == null) {
            return defaultValue;
        }
        return Double.parseDouble(str);
    }

    public String get(String key, String defaultValue) {
        return get(key, defaultValue, null);
    }

    public String get(String key, String defaultValue, String defaultComment) {
        if(!comments.containsKey(key)) {
            set(key, null, defaultComment);
        }
        if(values.containsKey(key)) {
            return values.get(key);
        }
        if(defaultValue != null) {
            set(key, defaultValue, null);
            return defaultValue;
        }
        return null;
    }

    public void set(String key, boolean value) {
        set(key, value, null);
    }

    public void set(String key, boolean value, String comment) {
        set(key, Boolean.toString(value), comment);
    }

    public void set(String key, int value) {
        set(key, value, null);
    }

    public void set(String key, int value, String comment) {
        set(key, Integer.toString(value), comment);
    }

    public void set(String key, double value) {
        set(key, value, null);
    }

    public void set(String key, double value, String comment) {
        set(key, Double.toString(value), comment);
    }

    public void set(String key, String value) {
        set(key, value, null);
    }

    public void set(String key, String value, String comment) {
        if(key == null || (value == null && comment == null)) {
            return;
        }
        setChanged(true);
        if(value != null) {
            values.put(key, value.trim());
        }
        if(comment != null) {
            comments.put(key, comment);
        }
    }

    public boolean isChanged() {
        return isChanged;
    }

    private void setChanged(boolean changed) {
        isChanged = changed;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
