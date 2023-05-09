/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.pl3x.map.core.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.management.openmbean.KeyAlreadyExistsException;
import net.pl3x.map.core.util.FileUtil;
import net.pl3x.map.core.world.Biome;
import net.pl3x.map.core.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BiomeRegistry extends Registry<@NonNull Biome> {
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, Integer> indexMap;

    public BiomeRegistry() {
        this.indexMap = new HashMap<>();
    }

    public void init(@NonNull World world) {
        Path file = world.getTilesDirectory().resolve("biomes.gz");
        if (!Files.exists(file)) {
            return;
        }
        try {
            TypeToken<Map<Integer, String>> token = new TypeToken<>() {
            };
            this.indexMap.putAll(GSON.fromJson(FileUtil.readGzip(file), token).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getNextIndex(String id) {
        int index = this.indexMap.getOrDefault(id, -1);
        if (index > -1) {
            return index;
        }
        int i = 0;
        while (true) {
            if (!this.indexMap.containsValue(i)) {
                this.indexMap.put(id, i);
                return i;
            }
            i++;
        }
    }

    public @NonNull Biome register(@NonNull String id, int color, int foliage, int grass, int water, Biome.@NonNull GrassModifier grassModifier) {
        if (has(id)) {
            throw new KeyAlreadyExistsException("Biome already registered: " + id);
        }
        return register(id, new Biome(getNextIndex(id), id, color, foliage, grass, water, grassModifier));
    }

    @Override
    public @NonNull Biome get(@NonNull String id) {
        return getOrDefault(id, Biome.DEFAULT);
    }

    public void saveToDisk(@NonNull World world) {
        Map<Integer, String> map = new HashMap<>();
        values().forEach(biome -> map.put(biome.index(), biome.getKey()));
        try {
            FileUtil.saveGzip(GSON.toJson(map), world.getTilesDirectory().resolve("biomes.gz"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
