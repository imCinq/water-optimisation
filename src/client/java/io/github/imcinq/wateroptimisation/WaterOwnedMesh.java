package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Owns the still-water vertices that are removed from a qualifying vanilla
 * translucent section. The CPU staging storage stays alive until the render
 * thread uploads it, then only the GPU vertex buffer is retained.
 */
public final class WaterOwnedMesh implements AutoCloseable {
	private final ByteBufferBuilder stagingBuffer;
	private MeshData meshData;
	private final int indexCount;
	private GpuBuffer vertexBuffer;
	private boolean closed;

	private WaterOwnedMesh(ByteBufferBuilder stagingBuffer, MeshData meshData) {
		this.stagingBuffer = stagingBuffer;
		this.meshData = meshData;
		this.indexCount = meshData.drawState().indexCount();
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Uploads the immutable compiled vertices on the render thread. A failure
	 * is surfaced to the caller so the session-level fail-closed path can
	 * rebuild the section with vanilla ownership.
	 */
	public synchronized boolean ensureUploaded() {
		if (this.closed) {
			return false;
		}
		if (this.vertexBuffer != null) {
			return true;
		}

		this.vertexBuffer = RenderSystem.getDevice().createBuffer(
				() -> "Water Optimisation owned water vertices",
				GpuBuffer.USAGE_VERTEX,
				this.meshData.vertexBuffer()
		);
		this.meshData.close();
		this.meshData = null;
		this.stagingBuffer.close();
		Diagnostics.recordFarWaterUpload();
		return true;
	}

	public synchronized GpuBuffer vertexBuffer() {
		return this.vertexBuffer;
	}

	public int indexCount() {
		return this.indexCount;
	}

	@Override
	public synchronized void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		if (this.vertexBuffer != null) {
			this.vertexBuffer.close();
			this.vertexBuffer = null;
		}
		if (this.meshData != null) {
			this.meshData.close();
			this.meshData = null;
		}
		this.stagingBuffer.close();
	}

	public static final class Builder implements AutoCloseable {
		private final ByteBufferBuilder stagingBuffer = new ByteBufferBuilder(4096);
		private final BufferBuilder builder = new BufferBuilder(
				this.stagingBuffer,
				PrimitiveTopology.QUADS,
				DefaultVertexFormat.BLOCK
		);
		private boolean closed;

		private Builder() {
		}

		public void addFace(
				float x0, float y0, float z0, float u0, float v0,
				float x1, float y1, float z1, float u1, float v1,
				float x2, float y2, float z2, float u2, float v2,
				float x3, float y3, float z3, float u3, float v3,
				int color, int lightCoords, boolean addBackFace
		) {
			if (this.closed) {
				throw new IllegalStateException("Water-owned mesh builder is closed");
			}

			float[] x = {x0, x1, x2, x3};
			float[] y = {y0, y1, y2, y3};
			float[] z = {z0, z1, z2, z3};
			float[] u = {u0, u1, u2, u3};
			float[] v = {v0, v1, v2, v3};
			this.addVertex(x[0], y[0], z[0], u[0], v[0], color, lightCoords);
			this.addVertex(x[1], y[1], z[1], u[1], v[1], color, lightCoords);
			this.addVertex(x[2], y[2], z[2], u[2], v[2], color, lightCoords);
			this.addVertex(x[3], y[3], z[3], u[3], v[3], color, lightCoords);
			if (addBackFace) {
				this.addVertex(x[3], y[3], z[3], u[3], v[3], color, lightCoords);
				this.addVertex(x[2], y[2], z[2], u[2], v[2], color, lightCoords);
				this.addVertex(x[1], y[1], z[1], u[1], v[1], color, lightCoords);
				this.addVertex(x[0], y[0], z[0], u[0], v[0], color, lightCoords);
			}
		}

		private void addVertex(
				float x, float y, float z, float u, float v, int color, int lightCoords
		) {
			VertexConsumer vertex = this.builder.addVertex(x, y, z);
			vertex.setColor(color)
					.setUv(u, v)
					.setLight(lightCoords)
					.setNormal(0.0F, 1.0F, 0.0F);
		}

		public WaterOwnedMesh build() {
			if (this.closed) {
				return null;
			}
			MeshData mesh = this.builder.build();
			this.closed = true;
			if (mesh == null) {
				this.stagingBuffer.close();
				return null;
			}
			return new WaterOwnedMesh(this.stagingBuffer, mesh);
		}

		@Override
		public void close() {
			if (!this.closed) {
				this.closed = true;
				this.stagingBuffer.close();
			}
		}
	}
}
