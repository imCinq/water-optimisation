#!/usr/bin/env bash

set -euo pipefail

metadata_file="src/main/resources/fabric.mod.json"
source_root="src"

if [[ ! -f "$metadata_file" ]]; then
	echo "Client-only audit failed: missing $metadata_file." >&2
	exit 1
fi

if ! grep -Eq '"environment"[[:space:]]*:[[:space:]]*"client"' "$metadata_file"; then
	echo "Client-only audit failed: mod environment is not client-only." >&2
	exit 1
fi

if grep -Eq '"(main|server|preLaunch)"[[:space:]]*:' "$metadata_file"; then
	echo "Client-only audit failed: a non-client entrypoint is declared." >&2
	exit 1
fi

# net.minecraft.network.chat.Component is local UI text in official mappings.
# The audit therefore rejects packet/buffer/listener namespaces rather than the
# entire network package.
forbidden_pattern='ClientPlayNetworking|ServerPlayNetworking|ClientPacket|ServerPacket|net\.minecraft\.network\.(protocol|Connection|FriendlyByteBuf|RegistryFriendlyByteBuf|Packet|PacketListener)|net\.minecraft\.server\.|ServerTickEvents|ServerLifecycleEvents|setBlockAndUpdate|setDeltaMovement|setVelocity|teleportTo|clickSlot|sendChat'

if grep -RInE \
	--include='*.java' \
	--include='*.kt' \
	"$forbidden_pattern" "$source_root"; then
	echo "Client-only audit failed: a forbidden networking, server, or gameplay mutation reference was found." >&2
	exit 1
fi

echo "Client-only boundary audit passed."
