// Smart Job Finder - JavaScript Utilities & Dynamic Job Matching Engine

// 1. Comprehensive Technical Skills Catalog (70+ in-demand skills categorized)
const SKILLS_CATALOG = [
    // Programming Languages
    { name: 'Java', category: 'Languages' },
    { name: 'Python', category: 'Languages' },
    { name: 'C', category: 'Languages' },
    { name: 'C++', category: 'Languages' },
    { name: 'C#', category: 'Languages' },
    { name: 'JavaScript', category: 'Languages' },
    { name: 'TypeScript', category: 'Languages' },
    { name: 'Go', category: 'Languages' },
    { name: 'Swift', category: 'Languages' },
    { name: 'Kotlin', category: 'Languages' },
    { name: 'PHP', category: 'Languages' },
    { name: 'Ruby', category: 'Languages' },
    { name: 'Rust', category: 'Languages' },
    { name: 'SQL', category: 'Languages' },
    { name: 'HTML', category: 'Languages' },
    { name: 'CSS', category: 'Languages' },

    // Web & Frontend
    { name: 'React', category: 'Frontend' },
    { name: 'Angular', category: 'Frontend' },
    { name: 'Vue.js', category: 'Frontend' },
    { name: 'Node.js', category: 'Frontend' },
    { name: 'Redux', category: 'Frontend' },
    { name: 'Next.js', category: 'Frontend' },
    { name: 'Bootstrap', category: 'Frontend' },
    { name: 'Tailwind CSS', category: 'Frontend' },
    { name: 'Web Development', category: 'Frontend' },
    { name: 'GraphQL', category: 'Frontend' },

    // Backend & Architecture
    { name: 'Spring Boot', category: 'Backend' },
    { name: 'Spring', category: 'Backend' },
    { name: 'Microservices', category: 'Backend' },
    { name: 'REST APIs', category: 'Backend' },
    { name: 'Django', category: 'Backend' },
    { name: 'Flask', category: 'Backend' },
    { name: 'Express.js', category: 'Backend' },
    { name: 'Hibernate', category: 'Backend' },
    { name: 'Distributed Systems', category: 'Backend' },
    { name: 'System Design', category: 'Backend' },

    // Databases & Big Data
    { name: 'MySQL', category: 'Database' },
    { name: 'PostgreSQL', category: 'Database' },
    { name: 'MongoDB', category: 'Database' },
    { name: 'Redis', category: 'Database' },
    { name: 'Kafka', category: 'Database' },
    { name: 'BigQuery', category: 'Database' },
    { name: 'Cassandra', category: 'Database' },
    { name: 'Oracle DB', category: 'Database' },
    { name: 'DBMS', category: 'Database' },
    { name: 'Snowflake', category: 'Database' },

    // Cloud & DevOps
    { name: 'AWS', category: 'Cloud' },
    { name: 'Azure', category: 'Cloud' },
    { name: 'GCP', category: 'Cloud' },
    { name: 'Cloud', category: 'Cloud' },
    { name: 'Docker', category: 'Cloud' },
    { name: 'Kubernetes', category: 'Cloud' },
    { name: 'Linux', category: 'Cloud' },
    { name: 'Git', category: 'Cloud' },
    { name: 'CI/CD', category: 'Cloud' },
    { name: 'Terraform', category: 'Cloud' },

    // Data Analytics & Machine Learning
    { name: 'Excel', category: 'Data' },
    { name: 'Power BI', category: 'Data' },
    { name: 'Tableau', category: 'Data' },
    { name: 'Pandas', category: 'Data' },
    { name: 'NumPy', category: 'Data' },
    { name: 'Machine Learning', category: 'Data' },
    { name: 'Deep Learning', category: 'Data' },
    { name: 'Data Visualization', category: 'Data' },
    { name: 'Statistics', category: 'Data' },
    { name: 'Data Structures', category: 'Data' },
    { name: 'Algorithms', category: 'Data' },
    { name: 'Selenium', category: 'Data' }
];

// State: Global Selected Skills Array
let selectedSkills = [];
let debounceTimer = null;
let activeSuggestionIndex = -1;

document.addEventListener('DOMContentLoaded', () => {
    console.log('⚡ Smart Job Finder loaded successfully.');

    // 1. Alert Banner Handler
    const urlParams = new URLSearchParams(window.location.search);
    const msg = urlParams.get('message');
    const err = urlParams.get('error');

    const alertBox = document.getElementById('alertBanner');
    if (alertBox) {
        if (msg) {
            alertBox.textContent = decodeURIComponent(msg);
            alertBox.className = 'alert-banner success';
            alertBox.style.display = 'block';
        } else if (err) {
            alertBox.textContent = decodeURIComponent(err);
            alertBox.className = 'alert-banner error';
            alertBox.style.display = 'block';
        }
    }

    // 2. Initialize Interactive Skills System
    initSkillsManager();

    // 3. Fallback for broken images
    bindImageFallbacks();

    // 4. Modal Dismiss Listeners
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeAiSkillModal();
            hideSuggestions();
        }
    });

    const overlay = document.getElementById('aiSkillModalOverlay');
    if (overlay) {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeAiSkillModal();
            }
        });
    }
});

// ========================================================
// Interactive Selected Skills & Autocomplete Manager
// ========================================================

function initSkillsManager() {
    const searchInput = document.getElementById('skillSearchInput');
    const hiddenSkillsInput = document.getElementById('skillsInput');
    const clearBtn = document.getElementById('btnClearSkills');
    const dropdown = document.getElementById('skillSuggestionsDropdown');
    const addSkillBtn = document.getElementById('btnAddSkill');
    const roleInput = document.getElementById('role');
    const expInput = document.getElementById('experience');
    const salaryInput = document.getElementById('salary');

    // Read initial skills from hidden input if pre-populated
    if (hiddenSkillsInput && hiddenSkillsInput.value) {
        const initial = hiddenSkillsInput.value.split(',').map(s => s.trim()).filter(Boolean);
        initial.forEach(s => {
            if (!hasSkill(s)) selectedSkills.push(s);
        });
    }
    renderSelectedSkills();

    // Wire up Add Skill button
    if (addSkillBtn) {
        addSkillBtn.onclick = function(e) {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            handleAddSkillFromInput();
        };
    }

    // Input typing & keyboard navigation
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearSkillFeedback();
            const query = e.target.value.trim();
            activeSuggestionIndex = -1;
            if (query.length > 0) {
                renderSuggestions(query);
            } else {
                hideSuggestions();
            }
        });

        searchInput.addEventListener('keydown', (e) => {
            const items = dropdown ? dropdown.querySelectorAll('.suggestion-item, .custom-skill-prompt') : [];

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (items.length > 0) {
                    activeSuggestionIndex = (activeSuggestionIndex + 1) % items.length;
                    highlightSuggestion(items);
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (items.length > 0) {
                    activeSuggestionIndex = (activeSuggestionIndex - 1 + items.length) % items.length;
                    highlightSuggestion(items);
                }
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (activeSuggestionIndex >= 0 && items[activeSuggestionIndex]) {
                    items[activeSuggestionIndex].click();
                } else {
                    handleAddSkillFromInput();
                }
            } else if (e.key === 'Escape') {
                hideSuggestions();
            }
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.skill-input-box-wrap')) {
                hideSuggestions();
            }
        });
    }

    // Clear All Skills button
    if (clearBtn) {
        clearBtn.onclick = function(e) {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            clearAllSkills();
        };
    }

    // Category Filter Buttons for Quick Chips
    const catButtons = document.querySelectorAll('.quick-cat-btn');
    if (catButtons.length > 0) {
        catButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                catButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const cat = btn.getAttribute('data-cat');
                filterQuickChipsByCategory(cat);
            });
        });
    }

    // Listen for changes on Role, Experience, and Salary for live dynamic job matching
    if (roleInput) roleInput.addEventListener('input', () => triggerDebouncedSearch());
    if (expInput) expInput.addEventListener('input', () => triggerDebouncedSearch());
    if (salaryInput) salaryInput.addEventListener('input', () => triggerDebouncedSearch());

    // Initial search to load matching jobs on page load
    triggerDebouncedSearch(50);
}

// ----------------------------------------------------
// Skill State Operations (Add, Remove, Toggle, Clear)
// ----------------------------------------------------

function hasSkill(skillName) {
    if (!skillName) return false;
    const lower = skillName.trim().toLowerCase();
    return selectedSkills.some(s => s.toLowerCase() === lower);
}

function handleAddSkillFromInput(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    const searchInput = document.getElementById('skillSearchInput');
    if (!searchInput) return;

    const raw = searchInput.value;
    const clean = raw.trim();

    if (!clean) {
        showSkillFeedback('Please enter a skill name (e.g. Java, Python, Power BI).', 'warning');
        searchInput.focus();
        return;
    }

    if (hasSkill(clean)) {
        showSkillFeedback(`"${clean}" is already added to your selected skills.`, 'info');
        flashExistingBadge(clean);
        searchInput.value = '';
        hideSuggestions();
        return;
    }

    clearSkillFeedback();
    addSkill(clean);
    searchInput.value = '';
    hideSuggestions();
    searchInput.focus();
}

function showSkillFeedback(message, type) {
    const box = document.getElementById('skillInputFeedback');
    if (box) {
        box.textContent = message;
        box.className = 'skill-input-feedback ' + (type || 'warning');
        box.style.display = 'block';
        setTimeout(() => {
            if (box.textContent === message) {
                box.style.display = 'none';
            }
        }, 4000);
    }
}

function clearSkillFeedback() {
    const box = document.getElementById('skillInputFeedback');
    if (box) box.style.display = 'none';
}

function handleQuickChipClick(element, event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    if (!element) return;
    const skillName = element.getAttribute('data-skill') || element.textContent.replace('+', '').replace('✓', '').trim();
    toggleSkill(skillName);
}

function addSkill(skillName) {
    if (!skillName || !skillName.trim()) return false;
    const clean = skillName.trim();

    if (hasSkill(clean)) {
        showSkillFeedback(`"${clean}" is already added to your selected skills.`, 'info');
        flashExistingBadge(clean);
        return false;
    }

    selectedSkills.push(clean);
    renderSelectedSkills();
    triggerDebouncedSearch();
    return true;
}

function removeSkill(skillName) {
    if (!skillName) return;
    const lower = skillName.trim().toLowerCase();
    selectedSkills = selectedSkills.filter(s => s.toLowerCase() !== lower);
    renderSelectedSkills();
    triggerDebouncedSearch();
}

function toggleSkill(skillName) {
    if (hasSkill(skillName)) {
        removeSkill(skillName);
    } else {
        addSkill(skillName);
    }
}

function clearAllSkills() {
    selectedSkills = [];
    renderSelectedSkills();
    clearSkillFeedback();
    triggerDebouncedSearch();
}

function flashExistingBadge(skillName) {
    const lower = skillName.toLowerCase();
    const badges = document.querySelectorAll('.selected-skill-badge');
    badges.forEach(b => {
        if (b.getAttribute('data-skill').toLowerCase() === lower) {
            b.style.transform = 'scale(1.2)';
            b.style.borderColor = '#ef4444';
            b.style.boxShadow = '0 0 14px rgba(239, 68, 68, 0.5)';
            setTimeout(() => {
                b.style.transform = '';
                b.style.borderColor = '';
                b.style.boxShadow = '';
            }, 400);
        }
    });
}

// ----------------------------------------------------
// Render Selected Skills Badges with (×) Remove Buttons
// ----------------------------------------------------

function renderSelectedSkills() {
    const container = document.getElementById('selectedSkillsList');
    const wrapper = document.getElementById('selectedSkillsWrapper');
    const countPill = document.getElementById('skillsCountPill');
    const clearBtn = document.getElementById('btnClearSkills');
    const hiddenInput = document.getElementById('skillsInput');

    if (!container) return;

    // Sync hidden form input
    if (hiddenInput) {
        hiddenInput.value = selectedSkills.join(', ');
    }

    // Update count pill
    if (countPill) {
        countPill.textContent = selectedSkills.length;
    }

    // Toggle clear button visibility
    if (clearBtn) {
        clearBtn.style.display = selectedSkills.length > 0 ? 'inline-block' : 'none';
    }

    // Toggle wrapper active styling
    if (wrapper) {
        if (selectedSkills.length > 0) {
            wrapper.classList.add('has-skills');
        } else {
            wrapper.classList.remove('has-skills');
        }
    }

    // Render badge chips
    if (selectedSkills.length === 0) {
        container.innerHTML = '<span class="selected-skills-empty">No skills selected yet. Click skills below or type above.</span>';
    } else {
        container.innerHTML = '';
        selectedSkills.forEach(skill => {
            const badge = document.createElement('span');
            badge.className = 'selected-skill-badge';
            badge.setAttribute('data-skill', skill);

            const label = document.createElement('span');
            label.className = 'skill-text';
            label.textContent = skill;

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'skill-remove-btn';
            removeBtn.innerHTML = '&times;';
            removeBtn.title = 'Remove ' + skill;
            removeBtn.setAttribute('aria-label', 'Remove ' + skill);
            removeBtn.onclick = function(e) {
                e.preventDefault();
                e.stopPropagation();
                removeSkill(skill);
            };

            badge.appendChild(label);
            badge.appendChild(removeBtn);
            container.appendChild(badge);
        });
    }

    // Update quick-chips selected states (ensure visual consistency)
    document.querySelectorAll('.quick-chip').forEach(chip => {
        const chipSkill = chip.getAttribute('data-skill') || chip.textContent.replace('+', '').trim();
        if (hasSkill(chipSkill)) {
            chip.classList.add('selected');
        } else {
            chip.classList.remove('selected');
        }
    });
}

// ----------------------------------------------------
// Autocomplete Suggestions Dropdown Engine
// ----------------------------------------------------

function renderSuggestions(query) {
    const dropdown = document.getElementById('skillSuggestionsDropdown');
    const searchInput = document.getElementById('skillSearchInput');
    if (!dropdown || !searchInput) return;

    const lowerQuery = query.toLowerCase();

    // Filter catalog
    const matches = SKILLS_CATALOG.filter(item =>
        item.name.toLowerCase().includes(lowerQuery)
    ).slice(0, 8);

    let html = '';

    matches.forEach(item => {
        const already = hasSkill(item.name);
        const escaped = escapeHtml(item.name);
        const highlighted = highlightMatch(escaped, query);

        html += `
            <div class="suggestion-item ${already ? 'already-added' : ''}" data-skill="${escaped}">
                <div>
                    <strong>${highlighted}</strong>
                    ${already ? '<span style="font-size:11px; margin-left:6px; color:#10b981; font-weight:700;">✓ Added</span>' : ''}
                </div>
                <span class="suggestion-category-tag">${escapeHtml(item.category)}</span>
            </div>
        `;
    });

    // Custom Skill Prompt if query is not an exact match
    const isExactMatch = matches.some(m => m.name.toLowerCase() === lowerQuery);
    if (!isExactMatch) {
        html += `
            <div class="custom-skill-prompt" data-custom="${escapeHtml(query)}">
                <span>➕ Add <strong>"${escapeHtml(query)}"</strong> as custom skill</span>
                <span style="font-size:11px; margin-left:auto; opacity:0.8;">(Click or Press Enter)</span>
            </div>
        `;
    }

    dropdown.innerHTML = html;
    dropdown.style.display = 'block';

    // Click handler for suggestion items
    dropdown.querySelectorAll('.suggestion-item').forEach(item => {
        item.addEventListener('click', () => {
            const skill = item.getAttribute('data-skill');
            addSkill(skill);
            searchInput.value = '';
            hideSuggestions();
            searchInput.focus();
        });
    });

    // Click handler for custom skill prompt
    const customPrompt = dropdown.querySelector('.custom-skill-prompt');
    if (customPrompt) {
        customPrompt.addEventListener('click', () => {
            const skill = customPrompt.getAttribute('data-custom');
            addSkill(skill);
            searchInput.value = '';
            hideSuggestions();
            searchInput.focus();
        });
    }
}

function highlightSuggestion(items) {
    items.forEach((item, idx) => {
        if (idx === activeSuggestionIndex) {
            item.classList.add('active');
            item.scrollIntoView({ block: 'nearest' });
        } else {
            item.classList.remove('active');
        }
    });
}

function highlightMatch(text, query) {
    const idx = text.toLowerCase().indexOf(query.toLowerCase());
    if (idx === -1) return text;
    return text.substring(0, idx) +
           '<span style="background:#fef08a; color:#854d0e; padding:1px 3px; border-radius:3px;">' +
           text.substring(idx, idx + query.length) +
           '</span>' +
           text.substring(idx + query.length);
}

function hideSuggestions() {
    const dropdown = document.getElementById('skillSuggestionsDropdown');
    if (dropdown) dropdown.style.display = 'none';
    activeSuggestionIndex = -1;
}

function filterQuickChipsByCategory(category) {
    const chips = document.querySelectorAll('.quick-chip');
    chips.forEach(chip => {
        const chipCat = chip.getAttribute('data-category');
        if (category === 'all' || chipCat === category) {
            chip.style.display = 'inline-flex';
        } else {
            chip.style.display = 'none';
        }
    });
}

// ========================================================
// Dynamic Real-time Skill-Based Job Matching
// ========================================================

function triggerDebouncedSearch(delay = 200) {
    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        fetchMatchingJobs();
    }, delay);
}

function fetchMatchingJobs() {
    const grid = document.getElementById('dynamicJobsGrid');
    const loading = document.getElementById('resultsLoading');
    const counterText = document.getElementById('matchingJobsCounter');
    const subtitleText = document.getElementById('matchingJobsSubtitle');
    const emptyCard = document.getElementById('noMatchesCard');

    if (!grid) return;

    if (loading) loading.style.display = 'block';

    const roleElem = document.getElementById('role');
    const expElem = document.getElementById('experience');
    const salElem = document.getElementById('salary');

    const role = roleElem ? roleElem.value.trim() : '';
    const experience = expElem ? expElem.value.trim() : '1';
    const salary = salElem ? salElem.value.trim() : '';

    const params = new URLSearchParams();
    params.append('format', 'json');
    params.append('skills', selectedSkills.join(', '));
    params.append('role', role);
    params.append('experience', experience);
    params.append('salary', salary);

    fetch('findJobs?' + params.toString(), {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(res => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(data => {
        if (loading) loading.style.display = 'none';
        renderDynamicJobCards(data.jobs || [], selectedSkills);
    })
    .catch(err => {
        console.warn('⚡ Dynamic job matching fetch fallback triggered:', err);
        if (loading) loading.style.display = 'none';
    });
}

function renderDynamicJobCards(jobs, skillsSelected) {
    const grid = document.getElementById('dynamicJobsGrid');
    const emptyCard = document.getElementById('noMatchesCard');
    const counterText = document.getElementById('matchingJobsCounter');
    const subtitleText = document.getElementById('matchingJobsSubtitle');

    if (!grid) return;

    if (counterText) {
        if (skillsSelected.length > 0) {
            counterText.textContent = `🎯 Matching Opportunities (${jobs.length} Found)`;
        } else {
            counterText.textContent = `⚡ All Verified Opportunities (${jobs.length} Available)`;
        }
    }

    if (subtitleText) {
        if (skillsSelected.length > 0) {
            subtitleText.textContent = `Sorted by skill overlap: jobs matching ${skillsSelected.join(', ')} appear first.`;
        } else {
            subtitleText.textContent = `Select or type skills above to filter and prioritize matching companies in real-time.`;
        }
    }

    if (!jobs || jobs.length === 0) {
        grid.innerHTML = '';
        if (emptyCard) {
            emptyCard.style.display = 'block';
            const emptyQueryText = document.getElementById('noMatchesQueryText');
            if (emptyQueryText) {
                emptyQueryText.textContent = skillsSelected.length > 0
                    ? `No openings matched your selected skills: "${skillsSelected.join(', ')}".`
                    : 'No openings found matching your criteria.';
            }
        }
        return;
    }

    if (emptyCard) emptyCard.style.display = 'none';

    let html = '';
    jobs.forEach(job => {
        const logoPath = job.logo || 'images/default-company.svg';
        const role = job.role || 'Software Engineer';
        const company = job.companyName || 'Technology Partner';
        const score = job.matchScore || 70;
        const tierClass = job.matchTierClass || 'match-medium';
        const tierLabel = job.matchCount > 0 ? `${job.matchCount} Skills Matched` : (job.matchTierLabel || 'Good Match');
        const applyUrl = job.applyUrl || '#';
        const displaySal = job.displaySalary || job.salary || 'Competitive';
        const exp = job.experience || '0-2';
        const desc = job.description || '';

        // Matched chips HTML
        let matchedHtml = '';
        if (job.matchedSkills && job.matchedSkills.length > 0) {
            matchedHtml = `
                <div class="skills-summary-block">
                    <div class="skills-summary-header matched-header">
                        <span>✅ Matched Strengths (${job.matchedSkills.length})</span>
                    </div>
                    <div class="skills-tags-wrapper">
                        ${job.matchedSkills.map(m => `<span class="skill-chip skill-chip-matched">${escapeHtml(m)}</span>`).join('')}
                    </div>
                </div>
            `;
        }

        // Missing chips HTML
        let missingHtml = '';
        if (job.missingSkills && job.missingSkills.length > 0) {
            missingHtml = `
                <div class="skills-summary-block">
                    <div class="skills-summary-header gap-header">
                        <span>💡 Recommended to Learn (${job.missingSkills.length})</span>
                    </div>
                    <div class="skills-tags-wrapper">
                        ${job.missingSkills.slice(0, 4).map(g => `<span class="skill-chip skill-chip-gap">${escapeHtml(g)}</span>`).join('')}
                    </div>
                </div>
            `;
        }

        html += `
            <div class="job-card">
                <div class="job-card-top">
                    <div class="logo-wrapper">
                        <img src="${escapeHtml(logoPath)}" alt="${escapeHtml(company)} Logo" class="company-logo" onerror="this.onerror=null;this.src='images/default-company.svg';">
                    </div>
                    <div class="company-info">
                        <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
                            <h3 class="company-name">${escapeHtml(company)}</h3>
                            <div class="ai-match-card-badge ${tierClass}" title="${score}% Compatibility with your profile">
                                <span class="ai-badge-sparkle">✨</span>
                                <span class="ai-badge-score">${score}%</span>
                                <span class="ai-badge-label">${tierLabel}</span>
                            </div>
                        </div>
                        <h4 class="job-role">${escapeHtml(role)}</h4>
                    </div>
                </div>

                <div class="ai-match-meter">
                    <div class="ai-match-meter-fill ${tierClass}" style="width: ${score}%;"></div>
                </div>

                <div class="job-meta-badges">
                    <span class="badge badge-salary">💰 ₹${escapeHtml(displaySal)} / yr</span>
                    <span class="badge badge-exp">💼 ${escapeHtml(exp)} yrs exp</span>
                    <span class="badge badge-verified">✓ Verified Opening</span>
                </div>

                ${desc ? `<p class="job-description-text">${escapeHtml(desc)}</p>` : ''}

                <div class="skills-container">
                    ${matchedHtml}
                    ${missingHtml}
                </div>

                <div class="job-card-actions">
                    <button type="button" class="btn btn-ai-analysis"
                            data-id="${job.id}"
                            data-company="${escapeHtml(company)}"
                            data-role="${escapeHtml(role)}"
                            data-skills="${escapeHtml(job.skills || '')}"
                            data-exp="${escapeHtml(exp)}"
                            data-salary="${escapeHtml(displaySal)}"
                            data-logo="${escapeHtml(logoPath)}"
                            data-score="${score}"
                            data-url="${escapeHtml(applyUrl)}"
                            data-user-skills="${escapeHtml(skillsSelected.join(', '))}"
                            data-target-role="${escapeHtml(document.getElementById('role') ? document.getElementById('role').value : '')}"
                            data-user-exp="${escapeHtml(document.getElementById('experience') ? document.getElementById('experience').value : '1')}"
                            onclick="openAiSkillModal(this)">
                        ✨ AI Skill Analysis
                    </button>
                    <a href="${escapeHtml(applyUrl)}" target="_blank" rel="noopener noreferrer" class="btn btn-apply">
                        Apply on ${escapeHtml(company)} ↗
                    </a>
                </div>
            </div>
        `;
    });

    grid.innerHTML = html;
    bindImageFallbacks();
}

function bindImageFallbacks() {
    const images = document.querySelectorAll('.company-logo');
    images.forEach(img => {
        img.addEventListener('error', function() {
            this.src = 'images/default-company.svg';
        });
    });
}

// ========================================================
// AI Skill Analysis Modal Controller
// ========================================================

function openAiSkillModal(btn) {
    const overlay = document.getElementById('aiSkillModalOverlay');
    if (!overlay) return;

    // Read attributes from button
    const companyId = btn.getAttribute('data-id') || '';
    const companyName = btn.getAttribute('data-company') || 'Technology Partner';
    const companyRole = btn.getAttribute('data-role') || 'Software Engineer';
    const companySkills = btn.getAttribute('data-skills') || '';
    const companyExp = btn.getAttribute('data-exp') || '0-2';
    const companySalary = btn.getAttribute('data-salary') || '';
    const companyLogo = btn.getAttribute('data-logo') || 'images/default-company.svg';
    const initialScore = btn.getAttribute('data-score') || '75';
    const applyUrl = btn.getAttribute('data-url') || '#';

    // Retrieve search preferences
    const userSkills = selectedSkills.length > 0 ? selectedSkills.join(', ') : (btn.getAttribute('data-user-skills') || '');
    const targetRole = (document.getElementById('role') && document.getElementById('role').value) || btn.getAttribute('data-target-role') || '';
    const userExp = (document.getElementById('experience') && document.getElementById('experience').value) || btn.getAttribute('data-user-exp') || '1';

    // Populate immediate header fields
    document.getElementById('modalCompanyName').textContent = companyName;
    document.getElementById('modalCompanyRole').textContent = companyRole;
    document.getElementById('modalCompanyLogo').src = companyLogo;
    document.getElementById('modalMetaExp').textContent = '💼 ' + companyExp + ' yrs exp';
    document.getElementById('modalMetaSalary').textContent = '💰 ₹' + companySalary + ' / yr';
    document.getElementById('modalScoreValue').textContent = initialScore + '%';

    const applyBtn = document.getElementById('modalApplyBtn');
    if (applyBtn) {
        applyBtn.href = applyUrl;
        applyBtn.textContent = 'Apply on ' + companyName + ' ↗';
    }

    // Show loading state and open modal
    document.getElementById('modalLoadingState').style.display = 'block';
    document.getElementById('modalContentArea').style.display = 'none';
    overlay.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // Fetch AI Analysis from Backend Servlet
    const params = new URLSearchParams();
    params.append('companyId', companyId);
    params.append('companyName', companyName);
    params.append('companyRole', companyRole);
    params.append('companySkills', companySkills);
    params.append('companyExp', companyExp);
    params.append('userSkills', userSkills);
    params.append('targetRole', targetRole);
    params.append('userExp', userExp);

    fetch('aiAnalysis', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
        body: params.toString()
    })
    .then(res => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(data => {
        renderAiAnalysisContent(data);
    })
    .catch(err => {
        console.warn('⚡ AI Analysis fetch fallback triggered:', err);
        renderClientFallback(companyName, companyRole, companySkills, userSkills, initialScore);
    })
    .finally(() => {
        document.getElementById('modalLoadingState').style.display = 'none';
        document.getElementById('modalContentArea').style.display = 'block';
    });
}

function renderAiAnalysisContent(data) {
    if (data.score) {
        document.getElementById('modalScoreValue').textContent = data.score + '%';
    }
    const engineTitle = document.getElementById('modalEngineTitle');
    const engineNotice = document.getElementById('modalEngineNotice');
    const engineIcon = document.getElementById('modalEngineIcon');

    if (data.status === 'gemini_ai') {
        engineTitle.textContent = data.engineTitle || 'Google Gemini 1.5 Flash (Generative AI)';
        engineNotice.textContent = data.engineNotice || 'Real-time contextual generative intelligence.';
        engineIcon.textContent = '✨';
        engineIcon.parentElement.parentElement.style.background = '#f5f3ff';
        engineIcon.parentElement.parentElement.style.borderColor = '#ddd6fe';
    } else {
        engineTitle.textContent = data.engineTitle || 'Smart Heuristic Career Engine (Deterministic Analysis)';
        engineNotice.textContent = data.engineNotice || 'Algorithmic matching based on technical skills & role qualifications.';
        engineIcon.textContent = '🧠';
        engineIcon.parentElement.parentElement.style.background = '#f8fafc';
        engineIcon.parentElement.parentElement.style.borderColor = '#e2e8f0';
    }

    document.getElementById('modalExplanation').textContent = data.explanation || 'Personalized technical fit evaluation.';
    document.getElementById('modalRoleFitBadge').textContent = '🎯 ' + (data.roleFit || 'Competitive Candidate Alignment');

    // Matched Skills
    const matchedContainer = document.getElementById('modalMatchedSkillsList');
    matchedContainer.innerHTML = '';
    if (data.matchingSkills && data.matchingSkills.length > 0) {
        data.matchingSkills.forEach(skill => {
            const chip = document.createElement('span');
            chip.className = 'skill-chip skill-chip-matched';
            chip.textContent = skill;
            matchedContainer.appendChild(chip);
        });
    } else {
        matchedContainer.innerHTML = '<span style="font-size:12px; color:#64748b; font-style:italic;">No direct skill overlap detected yet.</span>';
    }

    // Missing Skills
    const missingContainer = document.getElementById('modalMissingSkillsList');
    missingContainer.innerHTML = '';
    if (data.missingSkills && data.missingSkills.length > 0) {
        data.missingSkills.forEach(skill => {
            const chip = document.createElement('span');
            chip.className = 'skill-chip skill-chip-gap';
            chip.textContent = skill;
            missingContainer.appendChild(chip);
        });
    } else {
        missingContainer.innerHTML = '<span style="font-size:12px; color:#059669; font-weight:700;">🎉 Great job! You meet 100% of listed skill requirements!</span>';
    }

    // Learning Roadmap Suggestions
    const roadmapContainer = document.getElementById('modalRoadmapList');
    roadmapContainer.innerHTML = '';
    if (data.learningSuggestions && data.learningSuggestions.length > 0) {
        data.learningSuggestions.forEach((step, idx) => {
            const item = document.createElement('div');
            item.className = 'ai-roadmap-item';
            item.innerHTML = `
                <div class="roadmap-step-num">${idx + 1}</div>
                <div class="roadmap-step-text">${escapeHtml(step)}</div>
            `;
            roadmapContainer.appendChild(item);
        });
    }
}

function renderClientFallback(company, role, companySkills, userSkills, score) {
    const data = {
        score: score || 70,
        status: 'heuristic',
        engineTitle: 'Smart Career Heuristic Engine (Offline Mode)',
        engineNotice: 'Deterministic analysis based on skill graph and role qualifications.',
        roleFit: 'Solid Candidate Profile Alignment',
        explanation: 'Your profile satisfies key technical fundamentals for ' + company + '\'s ' + role + ' position. Addressing remaining specific tools will maximize candidate suitability.',
        matchingSkills: userSkills ? userSkills.split(',').map(s => s.trim()).filter(Boolean) : [],
        missingSkills: companySkills ? companySkills.split(',').map(s => s.trim()).filter(Boolean).slice(0, 3) : [],
        learningSuggestions: [
            'Architect a hands-on project demonstrating modern practical application of required technologies.',
            'Deepen core algorithmic and domain-specific problem solving tailored to ' + role + '.',
            'Publish your code repository with comprehensive architecture documentation and unit tests.'
        ]
    };
    renderAiAnalysisContent(data);
}

function closeAiSkillModal() {
    const overlay = document.getElementById('aiSkillModalOverlay');
    if (overlay) {
        overlay.style.display = 'none';
        document.body.style.overflow = '';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/&/g, '&amp;')
                      .replace(/</g, '&lt;')
                      .replace(/>/g, '&gt;')
                      .replace(/"/g, '&quot;')
                      .replace(/'/g, '&#39;');
}

// Explicit window bindings
window.selectedSkills = selectedSkills;
window.hasSkill = hasSkill;
window.addSkill = addSkill;
window.removeSkill = removeSkill;
window.toggleSkill = toggleSkill;
window.clearAllSkills = clearAllSkills;
window.handleQuickChipClick = handleQuickChipClick;
window.handleAddSkillFromInput = handleAddSkillFromInput;
window.showSkillFeedback = showSkillFeedback;
window.clearSkillFeedback = clearSkillFeedback;
window.renderSelectedSkills = renderSelectedSkills;
window.triggerDebouncedSearch = triggerDebouncedSearch;
window.openAiSkillModal = openAiSkillModal;
window.closeAiSkillModal = closeAiSkillModal;