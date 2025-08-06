package com.github.standobyte.jojo.client.rendertype;

import java.util.SequencedMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;

import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class CustomMultiBufferSource extends BufferSource {
	public RenderStateShard modification;

	public CustomMultiBufferSource(ByteBufferBuilder sharedBuffer,
			SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers, 
			RenderStateShard modification) {
		super(sharedBuffer, fixedBuffers);
		this.modification = modification;
	}
	
	@Override
	public void endBatch(RenderType renderType, BufferBuilder builder) {
		MeshData meshdata = builder.build();
		if (meshdata != null) {
			if (renderType.sortOnUpload()) {
				ByteBufferBuilder bytebufferbuilder = this.fixedBuffers.getOrDefault(renderType, this.sharedBuffer);
				meshdata.sortQuads(bytebufferbuilder, /*RenderSystem.getProjectionType().vertexSorting()*/RenderSystem.getVertexSorting());
			}

			renderType.setupRenderState();
			modification.setupRenderState();
	        BufferUploader.drawWithShader(meshdata);
	        modification.clearRenderState();
	        renderType.clearRenderState();
		}

		if (renderType.equals(this.lastSharedType)) {
			this.lastSharedType = null;
		}
	}

}
