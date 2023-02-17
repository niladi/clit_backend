package clit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

import structure.config.constants.EnumPipelineType;

public class APIPropertyLoader {
	private final String propertyFolder;

	public APIPropertyLoader(final String folder) {
		this.propertyFolder = folder;
		final List<APIComponent> propertiesAPIComponents = Lists.newArrayList();
	}

	public APIPropertyLoader() {
		this("/clit/properties");
	}

	public Collection<APIComponent> load() {
		return load(this.propertyFolder);
	}

	public Collection<APIComponent> load(final String file) {
		final List<APIComponent> propertiesAPIComponents = Lists.newArrayList();
		load(propertiesAPIComponents, file);
		return propertiesAPIComponents;
	}

	private Collection<APIComponent> load(final List<APIComponent> propertiesAPIComponents, final String file) {
		final File inFile = new File(file);
		if (inFile.isDirectory()) {
			System.out.println("It's a directory - going in!");
			for (final String f : inFile.list()) {
				System.out.println("Going into: " + inFile.getAbsolutePath() + "/" + f);
				load(propertiesAPIComponents, inFile.getAbsolutePath() + "/" + f);
			}
		} else if (inFile.isFile() && inFile.getName().endsWith(".properties")) {
			System.out.println("Found a file!");
			// Define keys:
			/**
			 * We need to know the 1. URL 2. Display name 3. What type of component it is...
			 * means for each you need to have a true/false
			 * 
			 */

			final String keyURL = "url", keyDisplayName = "displayname";

			final String valURL, valDisplayName;

			final Properties prop = new Properties();
			try {
				// load a properties file from class path, inside static method
				prop.load(new BufferedReader(new FileReader(inFile)));

				// get the URL
				valURL = prop.getProperty(keyURL);
				System.out.println("ValURL: " + valURL);
				// get the display name
				valDisplayName = prop.getProperty(keyDisplayName);
				System.out.println("val display name: " + valDisplayName);

				// the rest of the values are defined in EnumPipelineType.values()
				final List<EnumPipelineType> types = Lists.newArrayList();
				for (EnumPipelineType type : EnumPipelineType.values()) {
					if (Boolean.valueOf(prop.getProperty(type.getName()))) {
						System.out.println("Component is a: " + type);
						types.add(type);
					}
				}
				final APIComponent apiComponent = new APIComponent(valURL, valDisplayName, types);
				propertiesAPIComponents.add(apiComponent);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return propertiesAPIComponents;
	}
}
