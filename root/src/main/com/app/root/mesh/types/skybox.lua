meshType = "skybox"

function gen(radius, sectors, stacks)
    local vertices = {}
    local indices = {}
    local colors = {}

    local PI = math.pi
    local sectorStep = 2 * PI / sectors;
    local stackStep = PI / stacks;

    -- Vertices
    for i = 0, stacks do
        local stackAngle = PI / 2 - i * stackStep
        local xy = radius * math.cos(stackAngle)
        local z = radius * math.sin(stackAngle)

        for j = 0, sectors do
            local sectorAngle = j * sectorStep

            local x = xy * math.cos(sectorAngle)
            local y = xy * math.sin(sectorAngle)

            table.insert(vertices, x)
            table.insert(vertices, y)
            table.insert(vertices, z)
        end
    end
    -- Indices
    for i = 0, stacks - 1 do
        local k1 = i * (sectors + 1)
        local k2 = k1 + sectors + 1

        for j = 0, sectors - 1 do
            table.insert(indices, k1+j)
            table.insert(indices, k2+j)
            table.insert(indices, k1+j+1)

            table.insert(indices, k1+j+1)
            table.insert(indices, k2+j)
            table.insert(indices, k2+j+1)
        end
    end
    -- Colors
    for i = 1, #vertices / 3 * 4 do
        colors[i] = 0.0
    end

    return vertices, indices, colors
end
vertices, indices, colors = gen(1000.0, 36, 18)
-- Rotation
rotation = {
    axis = "Y",
    speed = 260.0
}
