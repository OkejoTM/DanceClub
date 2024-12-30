package core.services.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class JsonStorageService<T> {
    protected final Gson gson;
    protected final String dataFolder;
    protected final Class<T> typeParameterClass;
    protected Map<String, T> entities;
    private final Object fileLock = new Object();

    public JsonStorageService(Class<T> typeParameterClass, String dataFolder) {
        this.typeParameterClass = typeParameterClass;
        this.dataFolder = dataFolder;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
        this.entities = new ConcurrentHashMap<>();

        new File(dataFolder).mkdirs();
        loadData();
    }

    protected String getFilePath() {
        return dataFolder + File.separator + typeParameterClass.getSimpleName().toLowerCase() + "s.json";
    }

    private void loadData() {
        File file = new File(getFilePath());
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            TypeToken<HashMap<String, T>> typeToken = new TypeToken<>() {};
            Map<String, T> loadedData = gson.fromJson(reader, typeToken.getType());
            if (loadedData != null) {
                entities = new ConcurrentHashMap<>(loadedData);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data: " + e.getMessage());
        }
    }

    private void saveData() {
        synchronized (fileLock) {
            try (FileWriter writer = new FileWriter(getFilePath())) {
                gson.toJson(entities, writer);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save data: " + e.getMessage());
            }
        }
    }

    public void save(T entity) {
        String id = getId(entity);
        entities.put(id, entity);
        saveData();
    }

    public void saveAll(List<T> entityList) {
        for (T entity : entityList) {
            entities.put(getId(entity), entity);
        }
        saveData();
    }

    public T getById(String id) {
        return entities.get(id);
    }

    public List<T> getAll() {
        return new ArrayList<>(entities.values());
    }

    public void delete(String id) {
        entities.remove(id);
        saveData();
    }

    public void deleteAll() {
        entities.clear();
        saveData();
    }

    protected abstract String getId(T entity);
}

// Local Date Adapter (needed for both versions)
class LocalDateAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        jsonWriter.value(localDate.toString());
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString());
    }
}