package com.github.standobyte.jojo.client.entityanim.playerbend;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.github.standobyte.jojo.util.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.Util;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

// TODO (entity animation) fix model bends with smaller child cubes
// TODO (entity animation) fix model bends with StuckInBodyLayer
// TODO (entity animation) item holding position/rotation
// TODO (entity animation) parent xrot bones for limbs?
public class PlayerModelBends {
	
	public static float getLimbHeight(ModelPart limb) {
		if (limb.cubes.isEmpty()) {
			return 12;
		}
		ModelPart.Cube cube = limb.cubes.get(0);
		return cube.maxY - cube.minY;
	}
	
	public static ModelPart getModelPartForPlayerAnim(HumanoidModel<?> playerModel, String animBoneName) {
		return switch (animBoneName) {
			case "body" -> 				((IPlayerPseudoModelParts) playerModel).rotpAnimMainBody();
			case "torso" -> 			((IPlayerPseudoModelParts) playerModel).rotpAnimTorso();
			case "left_arm" -> 			playerModel.leftArm;
			case "right_arm" -> 		playerModel.rightArm;
			case "left_leg" ->			playerModel.leftLeg;
			case "right_leg" -> 		playerModel.rightLeg;
			case "head" -> 				playerModel.head;
			case "torso_bend" -> 		((IPlayerPseudoModelParts) playerModel).rotpAnimTorsoBend();
			case "left_arm_bend" -> 	((IPlayerPseudoModelParts) playerModel).rotpAnimLeftArmBend();
			case "right_arm_bend" -> 	((IPlayerPseudoModelParts) playerModel).rotpAnimRightArmBend();
			case "left_leg_bend" -> 	((IPlayerPseudoModelParts) playerModel).rotpAnimLeftLegBend();
			case "right_leg_bend" -> 	((IPlayerPseudoModelParts) playerModel).rotpAnimRightLegBend();
			case "leftItem" -> 			((IPlayerPseudoModelParts) playerModel).rotpAnimLeftItem();
			case "rightItem" -> 		((IPlayerPseudoModelParts) playerModel).rotpAnimRightItem();
			case "cape" -> 				playerModel.body.children.get("cape");
			case "cape_bend" -> 		((IPlayerPseudoModelParts) playerModel).rotpAnimCapeBend();
			default -> null;
		};
	}

	public static void renderWithBends(HumanoidModel<?> model, IPlayerPseudoModelParts animModel, 
			PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color) {
		poseStack.pushPose();
			ModelPart part = animModel.rotpAnimMainBody();
			part.translateAndRotate(poseStack);
			poseStack.translate(0, -part.getInitialPose().y() / 16, 0);
			
			model.leftLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
			model.rightLeg.render(poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.pushPose();
				part = animModel.rotpAnimTorso();
				poseStack.translate(0, -part.getInitialPose().y() / 16, 0);
				part.translateAndRotate(poseStack);
				
				part = animModel.rotpAnimTorsoBend();
				poseStack.translate(0, -part.getInitialPose().y() / 16, 0);
				poseStack.translate(part.x / 16.0F * 2, part.y / 16.0F * 2, part.z / 16.0F * 2);
				if (part.xRot != 0.0F || part.yRot != 0.0F || part.zRot != 0.0F) {
					poseStack.mulPose(new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot));
				}
				poseStack.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
				
				model.body.render(poseStack, buffer, packedLight, packedOverlay, color);
				model.head.render(poseStack, buffer, packedLight, packedOverlay, color);
				model.leftArm.render(poseStack, buffer, packedLight, packedOverlay, color);
				model.rightArm.render(poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
		poseStack.popPose();
	}
	
	
	
	public static void drawBentCubes(ModelPart limb, ModelPart bend, boolean invertBend, 
			float bendOffsetX, float bendOffsetY, float bendOffsetZ, 
			boolean skipDraw, PoseStack poseStack, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		if (!skipDraw) {
			if (invertBend) {
				bend.xRot = -bend.xRot;
			}
			for (ModelPart.Cube cube : limb.cubes) {
				renderBentPolygons(cube.polygons, poseStack, bend,
						bendOffsetX, bendOffsetY, bendOffsetZ, 
						buffer, packedLight, packedOverlay, color);
			}
			if (invertBend) {
				bend.xRot = -bend.xRot;
			}
		}
		
		// TODO (player anim) child elements with limb bends (clothes, stands)
		for (ModelPart modelpart : limb.children.values()) {
			modelpart.render(poseStack, buffer, packedLight, packedOverlay, color);
		}
	}
	
	// FIXME (player anim) use the main cube height (12 in case of players) instead of the individual cube heights for bending
	private static Vector3f dest = new Vector3f();
	private static void renderBentPolygons(ModelPart.Polygon[] polygons, PoseStack poseStack, ModelPart bend,
			float bendOffsetX, float bendOffsetY, float bendOffsetZ, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		for (ModelPart.Polygon polygon : polygons) {
			Vector3f normal = polygon.normal();
			
			ModelPart.Vertex[] vertices = polygon.vertices();
			int vert = vertices.length;
			MutablePolygon upperRectangle = vert == 4 ? RECTANGLE1 : new MutablePolygon(vert);
			MutablePolygon lowerRectangle = vert == 4 ? RECTANGLE2 : new MutablePolygon(vert);
			float bendY = bend.y / 16;
			float bendZ = bend.z / 16;
			boolean upperHalf = fillCutRectangle(vertices, upperRectangle, -Float.MAX_VALUE, bendY);
			boolean lowerHalf = fillCutRectangle(vertices, lowerRectangle, bendY, Float.MAX_VALUE);
			if (upperHalf && lowerHalf) {
				float width = Math.abs(vertices[0].pos().z / 16);
				float yDiff = width * MathUtil.tan(bend.xRot / 2);
				for (var vertex : upperRectangle.vertices) {
					if (vertex.y == bendY) {
						if (vertex.z - bendZ > 0) 	vertex.y -= yDiff;
						else						vertex.y += yDiff;
					}
				}
				for (var vertex : lowerRectangle.vertices) {
					if (vertex.y == bendY) {
						if (vertex.z - bendZ > 0) 	vertex.y += yDiff;
						else						vertex.y -= yDiff;
					}
				}
			}
			if (upperHalf) {
				renderPolygon(poseStack.last(), normal, upperRectangle.vertices, 
						buffer, packedLight, packedOverlay, color);
			}
			poseStack.pushPose();
			bend.translateAndRotate(poseStack);
			poseStack.translate(0, -bend.getInitialPose().y() / 16, 0);
			if (lowerHalf) {
				renderPolygon(poseStack.last(), normal, lowerRectangle.vertices, 
						buffer, packedLight, packedOverlay, color);
			}
			poseStack.popPose();
		}
	}
	
	private static boolean fillCutRectangle(ModelPart.Vertex[] vertices, MutablePolygon rectangle, float minLimitY, float maxLimitY) {
		BendVertex[] targetArr = rectangle.vertices;
		float minY = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float minV = Float.MAX_VALUE;
		float maxV = -Float.MAX_VALUE;
		for (int i = 0; i < vertices.length; i++) {
			ModelPart.Vertex vertex = vertices[i];
			BendVertex target = targetArr[i];
			Vector3f vertexPos = vertex.pos();
			target.x = vertexPos.x / 16.0F;
			target.y = vertexPos.y / 16.0F;
			target.z = vertexPos.z / 16.0F;
			target.u = vertex.u();
			target.v = vertex.v();
			minY = Math.min(minY, target.y);
			maxY = Math.max(maxY, target.y);
			minV = Math.min(minV, target.v);
			maxV = Math.max(maxV, target.v);
		}
		if (maxY < minLimitY || minY > maxLimitY) { // not render the faces that do not belong to this half of the limb
			return false;
		}
		for (BendVertex vertex : targetArr) {
			if (vertex.y < minLimitY) {
				vertex.y = minLimitY;
				vertex.wasChanged = true;
			}
			else if (vertex.y > maxLimitY) {
				vertex.y = maxLimitY;
				vertex.wasChanged = true;
			}
			else {
				vertex.wasChanged = false;
			}
			if (vertex.wasChanged) { // remap the UV accordingly
				float yRatio = (vertex.y - minY) / (maxY - minY);
				vertex.v = minV + (maxV - minV) * yRatio;
			}
			rectangle.minY = Math.max(minY, minLimitY);
			rectangle.maxY = Math.min(maxY, maxLimitY);
		}
		return true;
	}
	
	// XXX try to fix the bent quad rendering
	/* 
	 * it's drawing a rectangle/quad as two triangles split by a diagonal line, 
	 * when the quad is bent - both individual triangles are transformed correctly, 
	 * but the resulting quad looks broken
	 */
	private static void renderPolygon(PoseStack.Pose pose, Vector3f normalVec, BendVertex[] vertices, 
			VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		dest.set(0);
		Matrix4f poseMatrix = pose.pose();
		Vector3f normal = pose.transformNormal(normalVec, dest);
		float normalX = normal.x;
		float normalY = normal.y;
		float normalZ = normal.z;

		for (int i = 0; i < vertices.length; i++) {
			BendVertex vertex = vertices[i];
			Vector3f pos = poseMatrix.transformPosition(vertex.x, vertex.y, vertex.z, dest);
			buffer.addVertex(
				pos.x, pos.y, pos.z, color, 
				vertex.u, vertex.v, packedOverlay, packedLight, 
				normalX, normalY, normalZ
			);
		}
	}
	
	private static final MutablePolygon RECTANGLE1 = new MutablePolygon(4);
	private static final MutablePolygon RECTANGLE2 = new MutablePolygon(4);

	@ApiStatus.Internal
	public static class MutablePolygon {
		final BendVertex[] vertices;
		float minY;
		float maxY;
		
		MutablePolygon(int verticesCount) {
			this.vertices = Util.make(new BendVertex[verticesCount], arr -> {
				for (int i = 0; i < arr.length; i++) arr[i] = new BendVertex();
			});
		}
	}

	@ApiStatus.Internal
	public static class BendVertex {
		float x;
		float y;
		float z;
		float u;
		float v;
		boolean wasChanged;
	}
	
}
