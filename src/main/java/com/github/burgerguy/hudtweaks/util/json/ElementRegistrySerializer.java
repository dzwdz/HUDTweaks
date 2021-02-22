package com.github.burgerguy.hudtweaks.util.json;

import com.github.burgerguy.hudtweaks.hud.ElementRegistry;
import com.github.burgerguy.hudtweaks.hud.HTIdentifier;
import com.github.burgerguy.hudtweaks.hud.element.HudElementEntry;
import com.github.burgerguy.hudtweaks.hud.element.HudElementType;
import com.github.burgerguy.hudtweaks.hud.tree.AbstractTypeNodeEntry;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ElementRegistrySerializer implements JsonSerializer<ElementRegistry> {
	@Override
	public JsonElement serialize(ElementRegistry elementRegistry, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject registryObject = new JsonObject();

		for (HudElementType elementType : elementRegistry.getElementTypes()) {
			JsonObject elementTypeObject = new JsonObject();

			HTIdentifier activeIdentifier = elementType.getActiveEntry().getIdentifier();
			elementTypeObject.addProperty("activeEntry", activeIdentifier.getNamespace().toString() + ":" + activeIdentifier.getEntryName().toString());

			for (AbstractTypeNodeEntry abstractEntry : elementType.getRawEntryList()) {
				HudElementEntry entry = (HudElementEntry) abstractEntry;
				HTIdentifier identifier = entry.getIdentifier();
				elementTypeObject.add(identifier.getNamespace() + ":" + identifier.getEntryName(), entry.toJson(context));
			}
			registryObject.add(elementType.toString(), elementTypeObject);
		}

		return registryObject;
	}
	
	
}
