-- SionaCS Term API Extension
-- Adds CRT functions to redirected terminals

local expect = dofile("rom/modules/main/cc/expect.lua").expect

-- Store old redirect function
local oldRedirect = term.redirect

-- Track CRT status for redirected terminals
local terminalCRTStatus = {}
local nativeCRTEnabled = false

-- Get CRT status for a terminal
local function getCRTStatus(terminal)
    if terminal == term.native() then
        return nativeCRTEnabled
    else
        return terminalCRTStatus[terminal] or false
    end
end

-- Set CRT status for a terminal
local function setCRTStatus(terminal, enabled)
    if terminal == term.native() then
        nativeCRTEnabled = enabled
    else
        terminalCRTStatus[terminal] = enabled
    end
    return true
end

-- Custom implementation to ensure our methods are available
term.redirect = function(target)
    expect(1, target, "table")

    -- Add our custom methods if they don't exist
    if type(target.setCRT) ~= "function" then
        target.setCRT = function(enabled)
            return setCRTStatus(target, enabled)
        end
    end

    if type(target.getCRT) ~= "function" then
        target.getCRT = function()
            return getCRTStatus(target)
        end
    end

    -- Call original redirect
    local oldTerm = oldRedirect(target)

    -- Add methods to the old terminal too, if needed
    if type(oldTerm.setCRT) ~= "function" then
        oldTerm.setCRT = function(enabled)
            return setCRTStatus(oldTerm, enabled)
        end
    end

    if type(oldTerm.getCRT) ~= "function" then
        oldTerm.getCRT = function()
            return getCRTStatus(oldTerm)
        end
    end

    return oldTerm
end

-- Add CRT methods to the native terminal
if type(term.setCRT) ~= "function" then
    term.setCRT = function(enabled)
        return setCRTStatus(term.current(), enabled)
    end
end

if type(term.getCRT) ~= "function" then
    term.getCRT = function()
        return getCRTStatus(term.current())
    end
end

print("SionaCS: CRT Terminal Extensions loaded")
return true
